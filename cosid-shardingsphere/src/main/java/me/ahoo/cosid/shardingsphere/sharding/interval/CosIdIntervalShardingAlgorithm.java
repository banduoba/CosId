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

import me.ahoo.cosid.shardingsphere.sharding.CosIdAlgorithm;
import me.ahoo.cosid.util.LocalDateTimeConvert;

import com.google.common.base.Strings;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Properties;

/**
 * Interval-based time range sharding algorithm.
 *
 * @author ahoo wang
 */
public class CosIdIntervalShardingAlgorithm extends AbstractIntervalShardingAlgorithm<Comparable<?>> {
    public static final String TYPE = CosIdAlgorithm.TYPE_PREFIX + "INTERVAL";
    public static final String DATE_TIME_PATTERN_KEY = "datetime-pattern";
    public static final String TIMESTAMP_SECOND_UNIT = "SECOND";
    public static final String TIMESTAMP_UNIT_KEY = "ts-unit";
    private volatile boolean isSecondTs = false;
    private volatile DateTimeFormatter dateTimeFormatter;
    
    @Override
    public void init(final Properties props) {
        super.init(props);
        
        if (getProps().containsKey(TIMESTAMP_UNIT_KEY)
            && TIMESTAMP_SECOND_UNIT.equalsIgnoreCase(getProps().get(TIMESTAMP_UNIT_KEY).toString())) {
            isSecondTs = true;
        }
        final String dateTimePattern = getProps().getOrDefault(DATE_TIME_PATTERN_KEY, DEFAULT_DATE_TIME_PATTERN).toString();
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }
    
    @Override
    protected LocalDateTime convertShardingValue(final Comparable<?> shardingValue) {
        if (shardingValue instanceof LocalDateTime) {
            return (LocalDateTime) shardingValue;
        }
        if (shardingValue instanceof ZonedDateTime) {
            return ((ZonedDateTime) shardingValue).toLocalDateTime();
        }
        if (shardingValue instanceof OffsetDateTime) {
            return ((OffsetDateTime) shardingValue).toLocalDateTime();
        }
        if (shardingValue instanceof Instant) {
            return LocalDateTimeConvert.fromInstant((Instant) shardingValue, getZoneId());
        }
        if (shardingValue instanceof LocalDate) {
            return LocalDateTime.of((LocalDate) shardingValue, LocalTime.MIN);
        }
        if (shardingValue instanceof YearMonth) {
            YearMonth yearMonth = (YearMonth) shardingValue;
            return LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1, 0, 0);
        }
        if (shardingValue instanceof Year) {
            return LocalDateTime.of(((Year) shardingValue).getValue(), 1, 1, 0, 0);
        }
        if (shardingValue instanceof Date) {
            return LocalDateTimeConvert.fromDate((Date) shardingValue, getZoneId());
        }
        if (shardingValue instanceof Long) {
            if (isSecondTs) {
                return LocalDateTimeConvert.fromTimestampSecond((Long) shardingValue, getZoneId());
            }
            return LocalDateTimeConvert.fromTimestamp((Long) shardingValue, getZoneId());
        }
        
        if (shardingValue instanceof String) {
            return LocalDateTimeConvert.fromString((String) shardingValue, dateTimeFormatter);
        }
        throw new NotSupportIntervalShardingTypeException(Strings.lenientFormat("The current shard type:[%s] is not supported!", shardingValue.getClass()));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
