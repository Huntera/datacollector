/**
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.origin.httpserver;

import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.lib.http.HttpConfigs;
import com.streamsets.pipeline.lib.http.HttpReceiver;
import com.streamsets.pipeline.lib.httpsource.AbstractHttpServerPushSource;
import com.streamsets.pipeline.stage.origin.lib.DataParserFormatConfig;

import java.util.List;

public class HttpServerPushSource extends AbstractHttpServerPushSource<HttpReceiver> {

  private final HttpConfigs httpConfigs;
  private final int maxRequestSizeMB;

  private final DataFormat dataFormat;

  private final DataParserFormatConfig dataFormatConfig;

  public HttpServerPushSource(
      HttpConfigs httpConfigs,
      int maxRequestSizeMB,
      DataFormat dataFormat,
      DataParserFormatConfig dataFormatConfig
  ) {
    super(httpConfigs, new PushHttpReceiver(httpConfigs, maxRequestSizeMB, dataFormatConfig));
    this.httpConfigs = httpConfigs;
    this.maxRequestSizeMB = maxRequestSizeMB;
    this.dataFormat = dataFormat;
    this.dataFormatConfig = dataFormatConfig;
  }

  @Override
  protected List<ConfigIssue> init() {
    List<ConfigIssue> issues = getHttpConfigs().init(getContext());
    dataFormatConfig.init(
        getContext(),
        dataFormat,
        Groups.DATA_FORMAT.name(),
        "dataFormatConfig",
        maxRequestSizeMB * 1000 * 1000,
        issues
    );
    issues.addAll(getReceiver().init(getContext()));
    if (issues.isEmpty()) {
      issues.addAll(super.init());
    }
    return issues;
  }

  @Override
  public void destroy() {
    super.destroy();
    getReceiver().destroy();
    httpConfigs.destroy();
  }

}
