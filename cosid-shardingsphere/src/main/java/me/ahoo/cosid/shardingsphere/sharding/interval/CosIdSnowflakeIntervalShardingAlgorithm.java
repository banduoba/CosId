/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosid.shardingsphere.sharding.interval;

import me.ahoo.cosid.provider.IdGeneratorProvider;
import me.ahoo.cosid.provider.LazyIdGenerator;
import me.ahoo.cosid.shardingsphere.sharding.CosIdAlgorithm;

import com.google.common.base.Strings;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * The algorithm parses the timestamp from snowflake-id as the sharding value of Interval-based time range sharding algorithm.
 *
 * @author ahoo wang
 */
public class CosIdSnowflakeIntervalShardingAlgorithm extends AbstractIntervalShardingAlgorithm<Comparable<?>> {

    public static final String TYPE = AbstractIntervalShardingAlgorithm.TYPE_PREFIX + "SNOWFLAKE";

    private volatile LazyIdGenerator lazyIdGenerator;
    
    @Override
    public void init(final Properties props) {
        super.init(props);
        lazyIdGenerator = new LazyIdGenerator(getProps().getOrDefault(CosIdAlgorithm.ID_NAME_KEY, IdGeneratorProvider.SHARE).toString());
    }

    @Override
    protected LocalDateTime convertShardingValue(final Comparable<?> shardingValue) {
        Long snowflakeId = convertToSnowflakeId(shardingValue);
        return lazyIdGenerator.asFriendlyId(true).getParser().parseTimestamp(snowflakeId);
    }

    private Long convertToSnowflakeId(final Comparable<?> shardingValue) {
        if (shardingValue instanceof Long) {
            return (Long) shardingValue;
        }
        if (shardingValue instanceof String) {
            String shardingValueStr = (String) shardingValue;
            return lazyIdGenerator.idConverter().asLong(shardingValueStr);
        }
        throw new NotSupportIntervalShardingTypeException(Strings.lenientFormat("The current shard type:[%s] is not supported!", shardingValue.getClass()));
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
