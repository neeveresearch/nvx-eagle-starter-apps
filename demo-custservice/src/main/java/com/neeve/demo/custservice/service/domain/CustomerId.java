package com.neeve.demo.custservice.service.domain;

import com.neeve.service.IdentityInformationProvider;

import com.neeve.demo.custservice.state.Repository;
import com.neeve.demo.custservice.state.Partition;

final public class CustomerId {
    final private static int PARTITION_OFFSET = 0;
    final private static long PARTITION_MASK = 0x00000000000000ffl;
    final private static int COUNTER_OFFSET = 8;
    final private static long COUNTER_MASK = 0x7fffffffffffff00l;

    /**
     * Create a new customer id.
     *
     * @param partition An instance of a partition
     */
    final public static long create(final Partition partition) {
        if (partition == null) {
            throw new IllegalArgumentException("partition cannot be null");
        }
        partition.setNextCustomerId(partition.getNextCustomerId() + 1);
        return ((partition.getPartitionId() << PARTITION_OFFSET) & PARTITION_MASK) | ((partition.getNextCustomerId()<< COUNTER_OFFSET) & COUNTER_MASK);
    }

    /*
     * Create a new customer id.
     *
     * @param repository An instance of the running server's repository.
     */
    final public static long create(final Repository repository, final IdentityInformationProvider identityInformationProvider) {
        if (repository == null) {
            throw new IllegalArgumentException("repository cannot be null");
        }
        final int partitionId = identityInformationProvider.getPartition();
        Partition partition = repository.getPartitions().get((long)partitionId);
        if (partition == null) {
            partition = Partition.create();
            partition.setPartitionId(partitionId);
            repository.getPartitions().put((long)partitionId, partition);
        }
        return create(partition);
    }

    /**
     * Get the partition that owns a customer id.
     *
     * @param id The customer id whose partition is to be returned.
     */
    final public static int getPartition(final long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("invalid customer id '" + id + "'");
        }
        return (int)(id & PARTITION_MASK);
    }
}

