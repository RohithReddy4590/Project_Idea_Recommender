package com.projectrecommender.core.ports.outbound;

import java.util.List;

public interface EmbeddingServicePort {
    float[] getEmbedding(String text);
}
