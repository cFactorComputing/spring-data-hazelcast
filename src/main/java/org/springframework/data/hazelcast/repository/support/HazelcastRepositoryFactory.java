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
package org.springframework.data.hazelcast.repository.support;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;

import java.util.Optional;

/**
 * <p>
 * Hazelcast version of {@link KeyValueRepositoryFactory}, a factory to build {@link org.springframework.data.hazelcast.repository.HazelcastRepository} instances.
 * </P>
 * <p>
 * The purpose of extending is to ensure that the {@link #getQueryLookupStrategy} method returns a
 * {@link HazelcastQueryLookupStrategy} rather than the default.
 * </P>
 * <p>
 * The end goal of this bean is for {@link org.springframework.data.hazelcast.repository.query.HazelcastPartTreeQuery} to be used for query preparation.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastRepositoryFactory extends KeyValueRepositoryFactory {

    private static final Class<SpelQueryCreator> DEFAULT_QUERY_CREATOR = SpelQueryCreator.class;

    private final KeyValueOperations keyValueOperations;
    private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;

    /* Mirror functionality of super, to ensure private
     * fields are set.
     */
    public HazelcastRepositoryFactory(KeyValueOperations keyValueOperations) {
        this(keyValueOperations, DEFAULT_QUERY_CREATOR);
    }

    /* Capture KeyValueOperations and QueryCreator objects after passing to super.
     */
    public HazelcastRepositoryFactory(KeyValueOperations keyValueOperations,
                                      Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

        super(keyValueOperations, queryCreator);

        this.keyValueOperations = keyValueOperations;
        this.queryCreator = queryCreator;
    }

    /**
     * <p>
     * Ensure the mechanism for query evaluation is Hazelcast specific, as the original
     * {@link KeyValueRepositoryFactory.KeyValueQueryLookupStrategy} does not function correctly for Hazelcast.
     * </P>
     */
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
                                                                  EvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new HazelcastQueryLookupStrategy(key, evaluationContextProvider, this.keyValueOperations, this.queryCreator));
    }

}
