package net.jobsaddon.platform;

import net.jobsaddon.platform.services.IChunkDataHelper;
import net.jobsaddon.platform.services.INetworkHelper;
import net.jobsaddon.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static INetworkHelper network() {
        return NetworkHolder.NETWORK;
    }

    public static IChunkDataHelper chunkData() {
        return ChunkDataHolder.CHUNK_DATA;
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    private static final class NetworkHolder {
        private static final INetworkHelper NETWORK = load(INetworkHelper.class);
    }

    private static final class ChunkDataHolder {
        private static final IChunkDataHelper CHUNK_DATA = load(IChunkDataHelper.class);
    }
}
