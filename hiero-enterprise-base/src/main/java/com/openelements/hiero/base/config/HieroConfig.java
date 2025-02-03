package com.openelements.hiero.base.config;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.openelements.hiero.base.HieroContext;
import com.openelements.hiero.base.data.Account;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hiero configuration for one network connection.
 */
public interface HieroConfig {

    final static Logger log = LoggerFactory.getLogger(HieroConfig.class);

    /**
     * Returns the operator account for the network.
     *
     * @return the operator account
     */
    @NonNull
    Account getOperatorAccount();

    /**
     * Returns the network name.
     *
     * @return the network name
     */
    @NonNull
    Optional<String> getNetworkName();

    /**
     * Returns the mirror node addresses.
     *
     * @return the mirror node addresses
     */
    @NonNull
    Set<String> getMirrorNodeAddresses();

    /**
     * Returns the consensus nodes.
     *
     * @return the consensus nodes
     */
    @NonNull
    Set<ConsensusNode> getConsensusNodes();

    @NonNull
    Optional<Long> chainId();

    @NonNull
    Optional<String> relayUrl();

    /**
     * Creates a Hiero context. Calling this method multiple times will return a new instance each time.
     *
     * @return the Hiero context
     */
    @NonNull
    default HieroContext createHieroContext() {
        final Account operatorAccount = getOperatorAccount();
        final Client client = createClient();
        return new HieroContext() {
            @Override
            public @NonNull Account getOperatorAccount() {
                return operatorAccount;
            }

            @Override
            public @NonNull Client getClient() {
                return client;
            }
        };
    }

    /**
     * Creates a new client for the network. Calling this method multiple times will return a new instance each time.
     *
     * @return the client
     */
    @NonNull
    default Client createClient() {
        try {
            final Map<String, AccountId> nodes = getConsensusNodes().stream()
                    .collect(Collectors.toMap(n -> n.getAddress(), n -> n.getAccountId()));
            final Client client = Client.forNetwork(nodes);
            final List<String> mirrorNodeAddresses = getMirrorNodeAddresses().stream().collect(Collectors.toList());
            client.setMirrorNetwork(mirrorNodeAddresses);
            client.setOperator(getOperatorAccount().accountId(), getOperatorAccount().privateKey());
            return client;
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not create client for custom network", e);
        }
    }
}
