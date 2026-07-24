package com.wireguard.android.backend;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import com.resistine.android.network.flow.PacketDirection;
import com.resistine.android.network.flow.PacketTelemetrySink;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Peer;
import com.wireguard.crypto.Key;
import com.wireguard.crypto.KeyFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TelemetryGoBackend implements Backend {
    private static final int DNS_RESOLUTION_RETRIES = 10;
    private static final int TELEMETRY_BUFFER_BYTES = 65_536;
    private static final long TELEMETRY_JOIN_TIMEOUT_MS = 2_000L;
    private static final String TAG = "Resistine/GoBackend";

    @Nullable private static AlwaysOnCallback alwaysOnCallback;
    private static CompletableFuture<VpnService> vpnService = new CompletableFuture<>();

    static {
        System.loadLibrary("wg-go-telemetry");
    }

    private final Context context;
    private final PacketTelemetrySink telemetrySink;
    private final TelemetryHealthListener healthListener;

    @Nullable private Config currentConfig;
    @Nullable private Tunnel currentTunnel;
    @Nullable private TelemetrySession currentTelemetry;
    private int currentTunnelHandle = -1;

    public TelemetryGoBackend(
            final Context context,
            final PacketTelemetrySink telemetrySink,
            final TelemetryHealthListener healthListener) {
        this.context = context.getApplicationContext();
        this.telemetrySink = telemetrySink;
        this.healthListener = healthListener;
    }

    public static void setAlwaysOnCallback(final AlwaysOnCallback callback) {
        alwaysOnCallback = callback;
    }

    @Nullable private static native String wgGetConfig(int handle);

    private static native int wgGetSocketV4(int handle);

    private static native int wgGetSocketV6(int handle);

    private static native long wgGetTelemetryDrops(int handle);

    private static native long wgGetTelemetryQueueDepth(int handle);

    private static native long wgGetTelemetryQueueHighWater(int handle);

    private static native long wgTurnOff(int handle);

    private static native int wgTurnOn(
            String ifName,
            int tunFd,
            int outboundTelemetryFd,
            int inboundTelemetryFd,
            String settings);

    private static native String wgVersion();

    @Override
    public synchronized Set<String> getRunningTunnelNames() {
        if (currentTunnel == null) {
            return Collections.emptySet();
        }
        final Set<String> running = new ArraySet<>();
        running.add(currentTunnel.getName());
        return running;
    }

    @Override
    public synchronized Tunnel.State getState(final Tunnel tunnel) {
        return currentTunnel == tunnel ? Tunnel.State.UP : Tunnel.State.DOWN;
    }

    @Override
    public synchronized Statistics getStatistics(final Tunnel tunnel) {
        final Statistics statistics = new Statistics();
        if (tunnel != currentTunnel || currentTunnelHandle == -1) {
            return statistics;
        }
        final String config = wgGetConfig(currentTunnelHandle);
        if (config == null) {
            return statistics;
        }
        Key key = null;
        long rx = 0L;
        long tx = 0L;
        long latestHandshakeMillis = 0L;
        for (final String line : config.split("\\n")) {
            if (line.startsWith("public_key=")) {
                if (key != null) {
                    statistics.add(key, rx, tx, latestHandshakeMillis);
                }
                rx = 0L;
                tx = 0L;
                latestHandshakeMillis = 0L;
                try {
                    key = Key.fromHex(line.substring(11));
                } catch (final KeyFormatException ignored) {
                    key = null;
                }
            } else if (line.startsWith("rx_bytes=")) {
                rx = parseLong(line.substring(9));
            } else if (line.startsWith("tx_bytes=")) {
                tx = parseLong(line.substring(9));
            } else if (line.startsWith("last_handshake_time_sec=")) {
                latestHandshakeMillis += parseLong(line.substring(24)) * 1_000L;
            } else if (line.startsWith("last_handshake_time_nsec=")) {
                latestHandshakeMillis += parseLong(line.substring(25)) / 1_000_000L;
            }
        }
        if (key != null) {
            statistics.add(key, rx, tx, latestHandshakeMillis);
        }
        return statistics;
    }

    @Override
    public String getVersion() {
        return wgVersion();
    }

    public synchronized long getTelemetryQueueDepth() {
        return currentTunnelHandle == -1 ? 0L : wgGetTelemetryQueueDepth(currentTunnelHandle);
    }

    public synchronized long getTelemetryQueueHighWater() {
        return currentTunnelHandle == -1 ? 0L : wgGetTelemetryQueueHighWater(currentTunnelHandle);
    }

    public synchronized long getTelemetryDrops() {
        return currentTunnelHandle == -1 ? 0L : wgGetTelemetryDrops(currentTunnelHandle);
    }

    @Override
    public boolean isAlwaysOn() throws ExecutionException, InterruptedException, TimeoutException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false;
        }
        return vpnService.get(0, TimeUnit.NANOSECONDS).isAlwaysOn();
    }

    @Override
    public boolean isLockdownEnabled() throws ExecutionException, InterruptedException, TimeoutException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false;
        }
        return vpnService.get(0, TimeUnit.NANOSECONDS).isLockdownEnabled();
    }

    @Override
    public synchronized Tunnel.State setState(
            final Tunnel tunnel,
            Tunnel.State state,
            @Nullable final Config config) throws Exception {
        final Tunnel.State originalState = getState(tunnel);
        if (state == Tunnel.State.TOGGLE) {
            state = originalState == Tunnel.State.UP ? Tunnel.State.DOWN : Tunnel.State.UP;
        }
        if (state == originalState && tunnel == currentTunnel && config == currentConfig) {
            return originalState;
        }
        if (state == Tunnel.State.UP) {
            final Config originalConfig = currentConfig;
            final Tunnel originalTunnel = currentTunnel;
            if (currentTunnel != null) {
                setStateInternal(currentTunnel, null, Tunnel.State.DOWN);
            }
            try {
                setStateInternal(tunnel, config, Tunnel.State.UP);
            } catch (final Exception error) {
                if (originalTunnel != null) {
                    setStateInternal(originalTunnel, originalConfig, Tunnel.State.UP);
                }
                throw error;
            }
        } else if (state == Tunnel.State.DOWN && tunnel == currentTunnel) {
            setStateInternal(tunnel, null, Tunnel.State.DOWN);
        }
        return getState(tunnel);
    }

    private void setStateInternal(
            final Tunnel tunnel,
            @Nullable final Config config,
            final Tunnel.State state) throws Exception {
        Log.i(TAG, "Bringing tunnel " + tunnel.getName() + ' ' + state);
        if (state == Tunnel.State.UP) {
            bringUp(tunnel, config);
        } else {
            bringDown();
        }
        tunnel.onStateChange(state);
    }

    private void bringUp(final Tunnel tunnel, @Nullable final Config config) throws Exception {
        if (config == null) {
            throw new BackendException(BackendException.Reason.TUNNEL_MISSING_CONFIG);
        }
        if (android.net.VpnService.prepare(context) != null) {
            throw new BackendException(BackendException.Reason.VPN_NOT_AUTHORIZED);
        }

        final VpnService service = obtainVpnService();
        service.setOwner(this);
        if (currentTunnelHandle != -1) {
            Log.w(TAG, "Tunnel already up");
            return;
        }

        resolveEndpoints(config);
        final android.net.VpnService.Builder builder = service.getBuilder();
        builder.setSession(tunnel.getName());
        for (final String application : config.getInterface().getExcludedApplications()) {
            builder.addDisallowedApplication(application);
        }
        for (final String application : config.getInterface().getIncludedApplications()) {
            builder.addAllowedApplication(application);
        }
        for (final InetNetwork address : config.getInterface().getAddresses()) {
            builder.addAddress(address.getAddress(), address.getMask());
        }
        for (final InetAddress dns : config.getInterface().getDnsServers()) {
            builder.addDnsServer(dns.getHostAddress());
        }
        for (final String domain : config.getInterface().getDnsSearchDomains()) {
            builder.addSearchDomain(domain);
        }

        boolean sawDefaultRoute = false;
        for (final Peer peer : config.getPeers()) {
            for (final InetNetwork address : peer.getAllowedIps()) {
                if (address.getMask() == 0) {
                    sawDefaultRoute = true;
                }
                builder.addRoute(address.getAddress(), address.getMask());
            }
        }
        if (!(sawDefaultRoute && config.getPeers().size() == 1)) {
            builder.allowFamily(OsConstants.AF_INET);
            builder.allowFamily(OsConstants.AF_INET6);
        }
        builder.setMtu(config.getInterface().getMtu().orElse(1280));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false);
        }
        service.setUnderlyingNetworks(null);
        builder.setBlocking(true);

        final TelemetrySession telemetry = createTelemetrySession();
        int handle;
        try (ParcelFileDescriptor tun = builder.establish()) {
            if (tun == null) {
                telemetry.abort();
                throw new BackendException(BackendException.Reason.TUN_CREATION_ERROR);
            }
            Log.d(TAG, "Telemetry Go backend " + wgVersion());
            handle = wgTurnOn(
                    tunnel.getName(),
                    tun.detachFd(),
                    telemetry.detachOutboundNativeFd(),
                    telemetry.detachInboundNativeFd(),
                    config.toWgUserspaceString());
        }
        if (handle < 0) {
            telemetry.abort();
            throw new BackendException(BackendException.Reason.GO_ACTIVATION_ERROR_CODE, handle);
        }

        final int socketV4 = wgGetSocketV4(handle);
        final int socketV6 = wgGetSocketV6(handle);
        final boolean protectedV4 = socketV4 < 0 || service.protect(socketV4);
        final boolean protectedV6 = socketV6 < 0 || service.protect(socketV6);
        if (!protectedV4 || !protectedV6) {
            final long highWater = wgGetTelemetryQueueHighWater(handle);
            telemetry.prepareFinish();
            final long drops = wgTurnOff(handle);
            telemetry.finish();
            healthListener.onNativeQueueStats(0L, highWater);
            healthListener.onNativePacketDrops(drops);
            throw new BackendException(BackendException.Reason.GO_ACTIVATION_ERROR_CODE, -2);
        }

        currentTunnelHandle = handle;
        currentTunnel = tunnel;
        currentConfig = config;
        currentTelemetry = telemetry;
    }

    private void bringDown() {
        if (currentTunnelHandle == -1) {
            Log.w(TAG, "Tunnel already down");
            return;
        }
        final int handle = currentTunnelHandle;
        final TelemetrySession telemetry = currentTelemetry;
        final long highWater = wgGetTelemetryQueueHighWater(handle);
        currentTunnel = null;
        currentTunnelHandle = -1;
        currentConfig = null;
        currentTelemetry = null;
        if (telemetry != null) {
            telemetry.prepareFinish();
        }
        final long drops = wgTurnOff(handle);
        if (telemetry != null) {
            telemetry.finish();
        }
        healthListener.onNativeQueueStats(0L, highWater);
        healthListener.onNativePacketDrops(drops);
        try {
            vpnService.get(0, TimeUnit.NANOSECONDS).stopSelf();
        } catch (final TimeoutException ignored) {
            Log.w(TAG, "VPN service was unavailable during shutdown");
        } catch (final ExecutionException error) {
            Log.w(TAG, "VPN service shutdown failed", error);
        } catch (final InterruptedException error) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "VPN service shutdown interrupted", error);
        }
    }

    private VpnService obtainVpnService() throws Exception {
        if (!vpnService.isDone()) {
            context.startService(new Intent(context, VpnService.class));
        }
        try {
            return vpnService.get(2, TimeUnit.SECONDS);
        } catch (final TimeoutException error) {
            final BackendException backendError =
                    new BackendException(BackendException.Reason.UNABLE_TO_START_VPN);
            backendError.initCause(error);
            throw backendError;
        }
    }

    private void resolveEndpoints(final Config config) throws Exception {
        endpointRetry:
        for (int attempt = 0; attempt < DNS_RESOLUTION_RETRIES; attempt++) {
            for (final Peer peer : config.getPeers()) {
                final InetEndpoint endpoint = peer.getEndpoint().orElse(null);
                if (endpoint == null || endpoint.getResolved().orElse(null) != null) {
                    continue;
                }
                if (attempt < DNS_RESOLUTION_RETRIES - 1) {
                    Thread.sleep(1_000L);
                    continue endpointRetry;
                }
                throw new BackendException(
                        BackendException.Reason.DNS_RESOLUTION_FAILURE,
                        endpoint.getHost());
            }
            return;
        }
    }

    private TelemetrySession createTelemetrySession() {
        try {
            return TelemetrySession.create(telemetrySink, healthListener);
        } catch (final IOException error) {
            healthListener.onTelemetryReaderFailure(error);
            return TelemetrySession.disabled();
        }
    }

    private synchronized void handleServiceDestroyed() {
        final Tunnel tunnel = currentTunnel;
        if (tunnel == null || currentTunnelHandle == -1) {
            return;
        }
        final int handle = currentTunnelHandle;
        final TelemetrySession telemetry = currentTelemetry;
        final long highWater = wgGetTelemetryQueueHighWater(handle);
        currentTunnel = null;
        currentTunnelHandle = -1;
        currentConfig = null;
        currentTelemetry = null;
        if (telemetry != null) {
            telemetry.prepareFinish();
        }
        final long drops = wgTurnOff(handle);
        if (telemetry != null) {
            telemetry.finish();
        }
        healthListener.onNativeQueueStats(0L, highWater);
        healthListener.onNativePacketDrops(drops);
        tunnel.onStateChange(Tunnel.State.DOWN);
    }

    private static long parseLong(final String value) {
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ignored) {
            return 0L;
        }
    }

    public interface TelemetryHealthListener {
        void onNativePacketDrops(long count);

        void onTelemetryReaderFailure(Throwable error);

        default void onNativeQueueStats(long depth, long highWater) {
        }
    }

    public interface AlwaysOnCallback {
        void alwaysOnTriggered();
    }

    public static final class VpnService extends android.net.VpnService {
        @Nullable private TelemetryGoBackend owner;

        public Builder getBuilder() {
            return new Builder();
        }

        @Override
        public void onCreate() {
            vpnService.complete(this);
            super.onCreate();
        }

        @Override
        public void onDestroy() {
            if (owner != null) {
                owner.handleServiceDestroyed();
            }
            vpnService = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    ? vpnService.newIncompleteFuture()
                    : new CompletableFuture<>();
            super.onDestroy();
        }

        @Override
        public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
            vpnService.complete(this);
            if (intent == null
                    || intent.getComponent() == null
                    || !intent.getComponent().getPackageName().equals(getPackageName())) {
                if (alwaysOnCallback != null) {
                    alwaysOnCallback.alwaysOnTriggered();
                }
            }
            return super.onStartCommand(intent, flags, startId);
        }

        public void setOwner(final TelemetryGoBackend owner) {
            this.owner = owner;
        }
    }

    private static final class TelemetrySession {
        @Nullable private final TelemetryChannel outbound;
        @Nullable private final TelemetryChannel inbound;

        private TelemetrySession(
                @Nullable final TelemetryChannel outbound,
                @Nullable final TelemetryChannel inbound) {
            this.outbound = outbound;
            this.inbound = inbound;
        }

        static TelemetrySession create(
                final PacketTelemetrySink sink,
                final TelemetryHealthListener listener) throws IOException {
            TelemetryChannel outbound = null;
            try {
                outbound = TelemetryChannel.create(
                        "resistine-wg-outbound",
                        PacketDirection.OUTBOUND,
                        sink,
                        listener);
                final TelemetryChannel inbound = TelemetryChannel.create(
                        "resistine-wg-inbound",
                        PacketDirection.INBOUND,
                        sink,
                        listener);
                outbound.start();
                inbound.start();
                return new TelemetrySession(outbound, inbound);
            } catch (final IOException error) {
                if (outbound != null) {
                    outbound.abort();
                }
                throw error;
            }
        }

        static TelemetrySession disabled() {
            return new TelemetrySession(null, null);
        }

        int detachOutboundNativeFd() {
            return outbound == null ? -1 : outbound.detachNativeFd();
        }

        int detachInboundNativeFd() {
            return inbound == null ? -1 : inbound.detachNativeFd();
        }

        void finish() {
            prepareFinish();
            if (outbound != null) {
                outbound.finish();
            }
            if (inbound != null) {
                inbound.finish();
            }
        }

        void prepareFinish() {
            if (outbound != null) {
                outbound.prepareFinish();
            }
            if (inbound != null) {
                inbound.prepareFinish();
            }
        }

        void abort() {
            if (outbound != null) {
                outbound.abort();
            }
            if (inbound != null) {
                inbound.abort();
            }
        }
    }

    private static final class TelemetryChannel {
        private final ParcelFileDescriptor reader;
        private final ParcelFileDescriptor nativeEnd;
        private final PacketDirection direction;
        private final PacketTelemetrySink sink;
        private final TelemetryHealthListener listener;
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final AtomicBoolean stopping = new AtomicBoolean(false);
        private final Thread thread;

        private TelemetryChannel(
                final String threadName,
                final ParcelFileDescriptor reader,
                final ParcelFileDescriptor nativeEnd,
                final PacketDirection direction,
                final PacketTelemetrySink sink,
                final TelemetryHealthListener listener) {
            this.reader = reader;
            this.nativeEnd = nativeEnd;
            this.direction = direction;
            this.sink = sink;
            this.listener = listener;
            this.thread = new Thread(this::readLoop, threadName);
            this.thread.setDaemon(true);
        }

        static TelemetryChannel create(
                final String threadName,
                final PacketDirection direction,
                final PacketTelemetrySink sink,
                final TelemetryHealthListener listener) throws IOException {
            final ParcelFileDescriptor[] pair = ParcelFileDescriptor.createReliableSocketPair();
            return new TelemetryChannel(threadName, pair[0], pair[1], direction, sink, listener);
        }

        void start() {
            thread.start();
        }

        int detachNativeFd() {
            return nativeEnd.detachFd();
        }

        void prepareFinish() {
            stopping.set(true);
        }

        void finish() {
            prepareFinish();
            joinReader();
            closeQuietly(reader);
            closeQuietly(nativeEnd);
        }

        void abort() {
            stopping.set(true);
            running.set(false);
            closeQuietly(reader);
            closeQuietly(nativeEnd);
            joinThread(250L);
        }

        private void readLoop() {
            final byte[] packet = new byte[TELEMETRY_BUFFER_BYTES];
            try (InputStream input = new ParcelFileDescriptor.AutoCloseInputStream(reader)) {
                while (running.get()) {
                    final int length = input.read(packet);
                    if (length < 0) {
                        break;
                    }
                    if (length == 0) {
                        continue;
                    }
                    try {
                        sink.ingest(packet, length, direction, System.currentTimeMillis());
                    } catch (final RuntimeException error) {
                        listener.onTelemetryReaderFailure(error);
                    }
                }
            } catch (final IOException error) {
                if (running.get() && !stopping.get()) {
                    listener.onTelemetryReaderFailure(error);
                }
            } finally {
                running.set(false);
            }
        }

        private void joinReader() {
            joinThread(TELEMETRY_JOIN_TIMEOUT_MS);
            if (thread.isAlive()) {
                running.set(false);
                closeQuietly(reader);
                joinThread(250L);
            }
        }

        private void joinThread(final long timeoutMillis) {
            try {
                thread.join(timeoutMillis);
            } catch (final InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }

        private static void closeQuietly(final ParcelFileDescriptor descriptor) {
            try {
                descriptor.close();
            } catch (final IOException ignored) {
                // The native side may already own or have closed this descriptor.
            }
        }
    }
}
