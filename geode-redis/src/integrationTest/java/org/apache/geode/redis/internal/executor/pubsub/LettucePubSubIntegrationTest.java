/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.redis.internal.executor.pubsub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.geode.redis.GeodeRedisServerRule;
import org.apache.geode.test.awaitility.GeodeAwaitility;
import org.apache.geode.test.junit.rules.ExecutorServiceRule;

public class LettucePubSubIntegrationTest {

  private static final String CHANNEL = "best-channel";
  private static final String PATTERN = "best-*";
  protected RedisClient client;

  @ClassRule
  public static GeodeRedisServerRule server = new GeodeRedisServerRule();

  @ClassRule
  public static ExecutorServiceRule executor = new ExecutorServiceRule();

  @Before
  public void before() {
    client = RedisClient.create("redis://localhost:" + server.getPort());
  }

  @After
  public void after() {
    client.shutdown();
  }

  @Test
  public void multiSubscribeSameClient() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    StatefulRedisPubSubConnection<String, String> publisher = client.connectPubSub();
    List<Map> messages = Collections.synchronizedList(new ArrayList<>());

    RedisPubSubListener<String, String> listener = new RedisPubSubAdapter<String, String>() {
      @Override
      public void message(String channel, String message) {
        Map<String, String> channelMessageMap = new HashMap<>();
        channelMessageMap.put(channel, message);
        messages.add(channelMessageMap);
      }
    };

    subscriber.addListener(listener);
    subscriber.sync().subscribe(CHANNEL);
    subscriber.sync().subscribe(CHANNEL);
    subscriber.sync().subscribe("newChannel!");

    long publishCount1 = publisher.sync().publish(CHANNEL, "message!");
    long publishCount2 = publisher.sync().publish("newChannel!", "message from new channel");


    Map<String, String> expectedMap1 = new HashMap<>();
    expectedMap1.put(CHANNEL, "message!");
    Map<String, String> expectedMap2 = new HashMap<>();
    expectedMap2.put("newChannel!", "message from new channel");

    assertThat(publishCount1).isEqualTo(1);
    assertThat(publishCount2).isEqualTo(1);
    GeodeAwaitility.await().untilAsserted(() -> assertThat(messages).hasSize(2));
    assertThat(messages).containsExactly(expectedMap1, expectedMap2);

    subscriber.sync().unsubscribe();
  }

  @Test
  public void multiPsubscribeSameClient() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    StatefulRedisPubSubConnection<String, String> publisher = client.connectPubSub();
    List<Map> messages = Collections.synchronizedList(new ArrayList<>());

    RedisPubSubListener<String, String> listener = new RedisPubSubAdapter<String, String>() {
      @Override
      public void message(String pattern, String channel, String message) {
        Map<String, String> patternMessageMap = new HashMap<>();
        patternMessageMap.put(pattern, message);
        messages.add(patternMessageMap);
      }
    };

    subscriber.addListener(listener);
    subscriber.sync().psubscribe(PATTERN);
    subscriber.sync().psubscribe(PATTERN);
    subscriber.sync().psubscribe("new-*");

    long publishCount1 = publisher.sync().publish(CHANNEL, "message!");
    long publishCount2 = publisher.sync().publish("new-channel!", "message from new channel");


    Map<String, String> expectedMap1 = new HashMap<>();
    expectedMap1.put(PATTERN, "message!");
    Map<String, String> expectedMap2 = new HashMap<>();
    expectedMap2.put("new-*", "message from new channel");

    assertThat(publishCount1).isEqualTo(1);
    assertThat(publishCount2).isEqualTo(1);
    GeodeAwaitility.await().untilAsserted(() -> assertThat(messages).hasSize(2));
    assertThat(messages).containsExactly(expectedMap1, expectedMap2);

    subscriber.sync().unsubscribe();
  }

  @Test
  @Ignore("GEODE-8498")
  public void subscribePsubscribeSameClient() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    StatefulRedisPubSubConnection<String, String> publisher = client.connectPubSub();
    List<String> messages = Collections.synchronizedList(new ArrayList<>());

    RedisPubSubListener<String, String> listener = new RedisPubSubAdapter<String, String>() {
      @Override
      public void message(String channel, String message) {
        messages.add("message");
      }

      @Override
      public void message(String pattern, String channel, String message) {
        messages.add("pmessage");
      }
    };
    subscriber.addListener(listener);
    subscriber.sync().subscribe(CHANNEL);
    subscriber.sync().psubscribe("best-*");
    long publishCount = publisher.sync().publish(CHANNEL, "message!");


    assertThat(publishCount).isEqualTo(2);
    GeodeAwaitility.await().untilAsserted(() -> assertThat(messages).hasSize(2));
    assertThat(messages).containsExactly("message", "pmessage");

    subscriber.sync().unsubscribe();
  }

  @Test
  public void multiUnsubscribe() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    List<Map> counts = Collections.synchronizedList(new ArrayList<>());
    RedisPubSubListener<String, String> listener = new RedisPubSubAdapter<String, String>() {
      @Override
      public void unsubscribed(String channel, long remainingSubscriptions) {
        Map<String, Long> channelCount = new HashMap<>();
        channelCount.put(channel, remainingSubscriptions);
        counts.add(channelCount);
      }
    };
    subscriber.addListener(listener);
    subscriber.sync().subscribe(CHANNEL);
    subscriber.sync().subscribe("new-channel!");
    subscriber.sync().unsubscribe(CHANNEL);
    subscriber.sync().unsubscribe(CHANNEL);
    subscriber.sync().unsubscribe("new-channel!");

    Map<String, Long> expectedMap1 = new HashMap<>();
    expectedMap1.put(CHANNEL, 1L);
    Map<String, Long> expectedMap2 = new HashMap<>();
    expectedMap2.put(CHANNEL, 1L);
    Map<String, Long> expectedMap3 = new HashMap<>();
    expectedMap3.put("new-channel!", 0L);

    GeodeAwaitility.await().untilAsserted(() -> assertThat(counts).hasSize(3));
    assertThat(counts).containsExactly(expectedMap1, expectedMap2, expectedMap3);
  }

  @Test
  public void multiPunsubscribe() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    List<Map> counts = Collections.synchronizedList(new ArrayList<>());
    RedisPubSubListener<String, String> listener = new RedisPubSubAdapter<String, String>() {
      @Override
      public void punsubscribed(String pattern, long remainingSubscriptions) {
        Map<String, Long> patternCount = new HashMap<>();
        patternCount.put(pattern, remainingSubscriptions);
        counts.add(patternCount);
      }
    };

    subscriber.addListener(listener);
    subscriber.sync().psubscribe(PATTERN);
    subscriber.sync().psubscribe("new-*");
    subscriber.sync().punsubscribe(PATTERN);
    subscriber.sync().punsubscribe(PATTERN);
    subscriber.sync().punsubscribe("new-*");

    Map<String, Long> expectedMap1 = new HashMap<>();
    expectedMap1.put(PATTERN, 1L);
    Map<String, Long> expectedMap2 = new HashMap<>();
    expectedMap2.put(PATTERN, 1L);
    Map<String, Long> expectedMap3 = new HashMap<>();
    expectedMap3.put("new-*", 0L);

    GeodeAwaitility.await().untilAsserted(() -> assertThat(counts).hasSize(3));
    assertThat(counts).containsExactly(expectedMap1, expectedMap2, expectedMap3);
  }

  @Test
  public void unsubscribePunsubscribe() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    List<String> counts = Collections.synchronizedList(new ArrayList<>());
    RedisPubSubListener<String, String> listener = new RedisPubSubAdapter<String, String>() {
      @Override
      public void unsubscribed(String channel, long numUnsubscribed) {
        counts.add("unsubscribe");
      }

      @Override
      public void punsubscribed(String pattern, long numPunsubscribed) {
        counts.add("punsubscribe");
      }
    };
    subscriber.addListener(listener);
    subscriber.sync().subscribe(CHANNEL);
    subscriber.sync().psubscribe("best-*");
    subscriber.sync().unsubscribe(CHANNEL);
    subscriber.sync().punsubscribe("best-*");

    GeodeAwaitility.await().untilAsserted(() -> assertThat(counts).hasSize(2));
    assertThat(counts).containsExactly("unsubscribe", "punsubscribe");
  }

  // Lettuce does not currently allow PING while subscribed
  @Test
  public void pingWhileSubscribed() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    subscriber.sync().subscribe(CHANNEL);
    assertThatThrownBy(() -> subscriber.sync().ping())
        .hasMessageContaining("Command PING not allowed while subscribed");
  }

  @Test
  public void quitWhileSubscribe() {
    StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
    StatefulRedisPubSubConnection<String, String> publisher = client.connectPubSub();

    subscriber.sync().subscribe(CHANNEL);

    String quitResponse = subscriber.sync().quit();
    assertThat(quitResponse).isEqualTo("OK");

    long publishCount = publisher.sync().publish(CHANNEL, "hello there");
    assertThat(publishCount).isEqualTo(0L);
  }

  @Test
  public void concurrentPublishersToMultipleSubscribers_doNotLosePublishMessages()
      throws Exception {
    int subscriberCount = 50;
    int publisherCount = 10;
    int publishIterations = 10000;

    for (int i = 0; i < subscriberCount; i++) {
      StatefulRedisPubSubConnection<String, String> subscriber = client.connectPubSub();
      subscriber.sync().subscribe(CHANNEL);
    }

    List<Future<Long>> results = new ArrayList<>();
    for (int i = 0; i < publisherCount; i++) {
      int localI = i;
      results.add(executor.submit(() -> publish(localI, publishIterations)));
    }

    long publishCount = 0;
    for (Future<Long> r : results) {
      publishCount += r.get();
    }

    assertThat(publishCount).isEqualTo(subscriberCount * publisherCount * publishIterations);
  }

  private Long publish(int index, int iterationCount) throws Exception {
    StatefulRedisPubSubConnection<String, String> publisher = client.connectPubSub();
    long publishCount = 0;

    List<RedisFuture<Long>> results = new ArrayList<>();
    for (int i = 0; i < iterationCount; i++) {
      results.add(publisher.async().publish(CHANNEL, "message-" + index + "-" + i));
    }

    for (RedisFuture<Long> r : results) {
      publishCount += r.get();
    }

    return publishCount;
  }
}