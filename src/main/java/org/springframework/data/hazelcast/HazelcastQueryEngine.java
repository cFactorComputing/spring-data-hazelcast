/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hazelcast;

import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;

import org.springframework.data.hazelcast.repository.query.HazelcastCriteriaAccessor;
import org.springframework.data.hazelcast.repository.query.HazelcastSortAccessor;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.util.comparator.CompoundComparator;

/**
 * <p>
 * Implementation of {@code findBy*()} and {@code countBy*{}} queries.
 * </P>
 *
 * @author Christoph Strobl
 * @author Neil Stevenson
 */
public class HazelcastQueryEngine
        extends QueryEngine<HazelcastKeyValueAdapter, Predicate<?, ?>, Comparator<Entry<?, ?>>> {

    public HazelcastQueryEngine() {
        super(new HazelcastCriteriaAccessor(), new HazelcastSortAccessor());
    }

    /**
     * <p>
     * Construct the final query predicate for Hazelcast to execute, from the base query plus any paging and sorting.
     * </P>
     * <p>
     * Variations here allow the base query predicate to be omitted, sorting to be omitted, and paging to be omitted.
     * </P>
     *
     * @param criteria Search criteria, null means match everything
     * @param sort     Possibly null collation
     * @param offset   Start point of returned page, -1 if not used
     * @param rows     Size of page, -1 if not used
     * @param keyspace The map name
     * @return Results from Hazelcast
     */
    @Override
    public Collection<?> execute(final Predicate<?, ?> criteria, final Comparator<Entry<?, ?>> sort, final long offset,
                                 final int rows, final String keyspace) {

        Predicate<?, ?> predicateToUse = criteria;

        @SuppressWarnings({"unchecked", "rawtypes"})
        CompoundComparator<Entry<?, ?>> sortToUse = (CompoundComparator<Entry<?, ?>>) sort;

        if (rows > 0) {
            PagingPredicate pp = null;
            if (sortToUse.getComparatorCount() > 0) {
                pp = new PagingPredicate(predicateToUse, sortToUse, rows);
            } else {
                pp = new PagingPredicate(predicateToUse, rows);
            }

            long x = offset / rows;
            while (x > 0) {
                pp.nextPage();
                x--;
            }
            predicateToUse = pp;

        } else {
            if (sortToUse != null && sortToUse.getComparatorCount() > 0) {
                predicateToUse = new PagingPredicate(predicateToUse, sortToUse, Integer.MAX_VALUE);
            }
        }

        if (predicateToUse == null) {
            return this.getAdapter().getMap(keyspace).values();
        } else {
            return this.getAdapter().getMap(keyspace).values(predicateToUse);
        }

    }

    /**
     * <p>
     * Execute {@code countBy*()} queries against a Hazelcast map.
     * </P>
     *
     * @param criteria Predicate tAbstractTopologyITo use, not null
     * @param keyspace The map name
     * @return Results from Hazelcast
     */
    @Override
    public long count(final Predicate<?, ?> criteria, final String keyspace) {
        return this.getAdapter().getMap(keyspace).keySet(criteria).size();
    }

}
