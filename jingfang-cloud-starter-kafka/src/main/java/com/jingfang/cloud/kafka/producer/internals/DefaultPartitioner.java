package com.jingfang.cloud.kafka.producer.internals;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;

/**
 * @author Jin
 */
public class DefaultPartitioner extends org.apache.kafka.clients.producer.internals.DefaultPartitioner {
	private static final String SPLIT = ":";
	
	private static int toPositive(int number) {
		return number & 0x7fffffff;
	}

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
		int numPartitions = partitions.size();
		if (keyBytes == null) {
			return super.partition(topic, key, null, value, valueBytes, cluster);
		} else {
			if (key instanceof String && ((String) key).contains(SPLIT)) {
				String prefix = ((String) key).split(SPLIT)[0];
				return toPositive(Utils.murmur2(prefix.getBytes())) % numPartitions;
			} else {
				return toPositive(Utils.murmur2(keyBytes)) % numPartitions;
			}
		}
	}
}
