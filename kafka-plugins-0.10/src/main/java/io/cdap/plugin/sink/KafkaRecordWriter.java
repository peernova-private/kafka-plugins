/*
 * Copyright © 2018 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.sink;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

/**
 * Record writer to write events to kafka
 */
public class KafkaRecordWriter extends RecordWriter<BytesWritable, BytesWritable> {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaRecordWriter.class);
  private KafkaProducer<String, String> producer;
  private String topic;

  public KafkaRecordWriter(KafkaProducer<String, String> producer, String topic) {
    this.producer = producer;
    this.topic = topic;
  }

  @Override
  public void write(BytesWritable key, BytesWritable value) throws IOException, InterruptedException {
    if (key == null) {
      sendMessage(null, value.getBytes());
    } else {
      sendMessage(key.getBytes(), value.getBytes());
    }
  }

  @Override
  public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
    if (producer != null) {
      producer.close();
    }
  }

  private void sendMessage(final byte[] key, final byte[] body) throws IOException, InterruptedException {
    try {
      LOG.info("encoded body=" + Base64.getEncoder().encodeToString(body));
      producer.send(new ProducerRecord(topic, key, body)).get();
    } catch (ExecutionException e) {
      throw new IOException(e.getCause());
    }
  }
}
