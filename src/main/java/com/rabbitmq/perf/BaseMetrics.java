// Copyright (c) 2018-2022 VMware, Inc. or its affiliates.  All rights reserved.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 2.0 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.

package com.rabbitmq.perf;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.MicrometerMetricsCollector;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.Collections;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.Collection;

import static com.rabbitmq.perf.Utils.strArg;

/**
 *
 */
public class BaseMetrics implements Metrics {

    @Override
    public Options options() {
        Options options = new Options();
        options.addOption(new Option("mt", "metrics-tags", true, "metrics tags as key-value pairs separated by commas"));
        options.addOption(new Option("mpx", "metrics-prefix", true, "prefix for PerfTest metrics, default is perftest_"));
        options.addOption(new Option("mc", "metrics-client", false, "enable client metrics"));
        options.addOption(new Option("mcl", "metrics-class-loader", false, "enable JVM class loader metrics"));
        options.addOption(new Option("mjm", "metrics-jvm-memory", false, "enable JVM memory metrics"));
        options.addOption(new Option("mjgc", "metrics-jvm-gc", false, "enable JVM GC metrics"));
        options.addOption(new Option("mjp", "metrics-processor", false, "enable processor metrics (gathered by JVM)"));
        options.addOption(new Option("mjt", "metrics-jvm-thread", false, "enable JVM thread metrics"));

        return options;
    }

    @Override
    public void configure(CommandLineProxy cmd, CompositeMeterRegistry meterRegistry, ConnectionFactory factory) {
        String argumentTags = strArg(cmd, "mt", null);
        meterRegistry.config().commonTags(parseTags(argumentTags));
        if (cmd.hasOption("mc")) {
            factory.setMetricsCollector(new MicrometerMetricsCollector(meterRegistry, "client"));
        }

        if (cmd.hasOption("mcl")) {
            new ClassLoaderMetrics().bindTo(meterRegistry);
        }
        if (cmd.hasOption("mjm")) {
            new JvmMemoryMetrics().bindTo(meterRegistry);
        }
        if (cmd.hasOption("mjgc")) {
            new JvmGcMetrics().bindTo(meterRegistry);
        }
        if (cmd.hasOption("mjp")) {
            new ProcessorMetrics().bindTo(meterRegistry);
        }
        if (cmd.hasOption("mjt")) {
            new JvmThreadMetrics().bindTo(meterRegistry);
        }
    }

    @Override
    public String toString() {
        return "Base Metrics";
    }

    static Collection<Tag> parseTags(String argument) {
        if (argument == null || argument.trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            Collection<Tag> tags = new ArrayList<>();
            for (String tag : argument.split(",")) {
                String[] keyValue = tag.split("=", 2);
                tags.add(Tag.of(keyValue[0], keyValue[1]));
            }
            return tags;
        }
    }
}
