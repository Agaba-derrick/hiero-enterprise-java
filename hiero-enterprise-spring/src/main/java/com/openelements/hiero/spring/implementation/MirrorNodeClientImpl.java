package com.openelements.hiero.spring.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.TokenId;
import com.openelements.hiero.base.HieroException;
import com.openelements.hiero.base.data.Nft;
import com.openelements.hiero.base.data.NftMetadata;
import com.openelements.hiero.base.data.Page;
import com.openelements.hiero.base.data.TransactionInfo;
import com.openelements.hiero.base.implementation.AbstractMirrorNodeClient;
import com.openelements.hiero.base.implementation.MirrorNodeJsonConverter;
import com.openelements.hiero.base.implementation.MirrorNodeRestClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.springframework.web.client.RestClient;

public class MirrorNodeClientImpl extends AbstractMirrorNodeClient<JsonNode> {

    private final ObjectMapper objectMapper;

    private final RestClient restClient;

    private final MirrorNodeRestClientImpl mirrorNodeRestClient;

    private final MirrorNodeJsonConverter<JsonNode> jsonConverter;

    /**
     * Constructor.
     *
     * @param restClientBuilder the builder for the REST client that must have the base URL set
     */
    public MirrorNodeClientImpl(final RestClient.Builder restClientBuilder) {
        Objects.requireNonNull(restClientBuilder, "restClientBuilder must not be null");
        mirrorNodeRestClient = new MirrorNodeRestClientImpl(restClientBuilder);
        jsonConverter = new MirrorNodeJsonConverterImpl();
        objectMapper = new ObjectMapper();
        restClient = restClientBuilder.build();
    }

    @Override
    protected final MirrorNodeRestClient<JsonNode> getRestClient() {
        return mirrorNodeRestClient;
    }

    @Override
    protected final MirrorNodeJsonConverter<JsonNode> getJsonConverter() {
        return jsonConverter;
    }

    @Override
    public Page<Nft> queryNftsByAccount(@NonNull final AccountId accountId) throws HieroException {
        Objects.requireNonNull(accountId, "newAccountId must not be null");
        final String path = "/api/v1/accounts/" + accountId + "/nfts";
        final Function<JsonNode, List<Nft>> dataExtractionFunction = node -> jsonConverter.toNfts(node);
        return new RestBasedPage<>(objectMapper, restClient.mutate().clone(), path, dataExtractionFunction);
    }

    @Override
    public Page<Nft> queryNftsByAccountAndTokenId(@NonNull final AccountId accountId, @NonNull final TokenId tokenId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        final String path = "/api/v1/tokens/" + tokenId + "/nfts/?account.id=" + accountId;
        final Function<JsonNode, List<Nft>> dataExtractionFunction = node -> jsonConverter.toNfts(node);
        return new RestBasedPage<>(objectMapper, restClient.mutate().clone(), path, dataExtractionFunction);
    }

    @Override
    public Page<Nft> queryNftsByTokenId(@NonNull TokenId tokenId) {
        final String path = "/api/v1/tokens/" + tokenId + "/nfts";
        final Function<JsonNode, List<Nft>> dataExtractionFunction = node -> jsonConverter.toNfts(node);
        return new RestBasedPage<>(objectMapper, restClient.mutate().clone(), path, dataExtractionFunction);
    }

    @Override
    public Page<TransactionInfo> queryTransactionsByAccount(@NonNull final AccountId accountId) throws HieroException {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = "/api/v1/transactions?account.id=" + accountId;
        final Function<JsonNode, List<TransactionInfo>> dataExtractionFunction = n -> jsonConverter.toTransactionInfos(
                n);
        return new RestBasedPage<>(objectMapper, restClient.mutate().clone(), path, dataExtractionFunction);
    }

    @Override
    public Optional<TransactionInfo> queryTransaction(@NonNull final String transactionId) throws HieroException {
        final JsonNode jsonNode = mirrorNodeRestClient.queryTransaction(transactionId);
        //TODO: I assume there is a better check
        if (jsonNode == null || !jsonNode.fieldNames().hasNext()) {
            return Optional.empty();
        }
        return Optional.of(new TransactionInfo(transactionId));
    }

    @Override
    public @NonNull Page<NftMetadata> findNftTypesByOwner(AccountId ownerId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public @NonNull Page<NftMetadata> findAllNftTypes() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
