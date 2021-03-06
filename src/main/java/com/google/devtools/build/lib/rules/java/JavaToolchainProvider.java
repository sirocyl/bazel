// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.rules.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.analysis.FilesToRunProvider;
import com.google.devtools.build.lib.analysis.RuleContext;
import com.google.devtools.build.lib.analysis.TransitiveInfoCollection;
import com.google.devtools.build.lib.analysis.configuredtargets.RuleConfiguredTarget.Mode;
import com.google.devtools.build.lib.analysis.platform.ToolchainInfo;
import com.google.devtools.build.lib.cmdline.Label;
import com.google.devtools.build.lib.collect.nestedset.NestedSet;
import com.google.devtools.build.lib.concurrent.ThreadSafety.Immutable;
import com.google.devtools.build.lib.events.Location;
import com.google.devtools.build.lib.packages.RuleErrorConsumer;
import java.util.List;
import javax.annotation.Nullable;

/** Information about the JDK used by the <code>java_*</code> rules. */
@Immutable
public class JavaToolchainProvider extends ToolchainInfo {

  /** Returns the Java Toolchain associated with the rule being analyzed or {@code null}. */
  public static JavaToolchainProvider from(RuleContext ruleContext) {
    return from(ruleContext, ":java_toolchain");
  }

  public static JavaToolchainProvider from(
      RuleContext ruleContext, String attributeName) {
    TransitiveInfoCollection prerequisite = ruleContext.getPrerequisite(attributeName, Mode.TARGET);
    return from(prerequisite, ruleContext);
  }

  public static JavaToolchainProvider from(TransitiveInfoCollection collection) {
    return from(collection, null);
  }

  private static JavaToolchainProvider from(
      TransitiveInfoCollection collection, @Nullable RuleErrorConsumer errorConsumer) {
    ToolchainInfo toolchainInfo = collection.get(ToolchainInfo.PROVIDER);
    if (toolchainInfo instanceof JavaToolchainProvider) {
      return (JavaToolchainProvider) toolchainInfo;
    }

    if (errorConsumer != null) {
      errorConsumer.ruleError("The selected Java toolchain is not a JavaToolchainProvider");
    }
    return null;
  }

  public static JavaToolchainProvider create(
      Label label,
      JavaToolchainData data,
      NestedSet<Artifact> bootclasspath,
      NestedSet<Artifact> extclasspath,
      List<String> defaultJavacFlags,
      Artifact javac,
      NestedSet<Artifact> tools,
      FilesToRunProvider javaBuilder,
      @Nullable Artifact headerCompiler,
      boolean forciblyDisableHeaderCompilation,
      Artifact singleJar,
      @Nullable Artifact oneVersion,
      @Nullable Artifact oneVersionWhitelist,
      Artifact genClass,
      @Nullable Artifact resourceJarBuilder,
      @Nullable Artifact timezoneData,
      FilesToRunProvider ijar,
      ImmutableListMultimap<String, String> compatibleJavacOptions) {
    return new JavaToolchainProvider(
        label,
        data.getSourceVersion(),
        data.getTargetVersion(),
        bootclasspath,
        extclasspath,
        data.getEncoding(),
        javac,
        tools,
        javaBuilder,
        headerCompiler,
        forciblyDisableHeaderCompilation,
        singleJar,
        oneVersion,
        oneVersionWhitelist,
        genClass,
        resourceJarBuilder,
        timezoneData,
        ijar,
        compatibleJavacOptions,
        // merges the defaultJavacFlags from
        // {@link JavaConfiguration} with the flags from the {@code java_toolchain} rule.
        ImmutableList.<String>builder()
            .addAll(data.getJavacOptions())
            .addAll(defaultJavacFlags)
            .build(),
        data.getJvmOptions(),
        data.getJavacSupportsWorkers());
  }

  private final Label label;
  private final String sourceVersion;
  private final String targetVersion;
  private final NestedSet<Artifact> bootclasspath;
  private final NestedSet<Artifact> extclasspath;
  private final String encoding;
  private final Artifact javac;
  private final NestedSet<Artifact> tools;
  private final FilesToRunProvider javaBuilder;
  @Nullable private final Artifact headerCompiler;
  private final boolean forciblyDisableHeaderCompilation;
  private final Artifact singleJar;
  @Nullable private final Artifact oneVersion;
  @Nullable private final Artifact oneVersionWhitelist;
  private final Artifact genClass;
  @Nullable private final Artifact resourceJarBuilder;
  @Nullable private final Artifact timezoneData;
  private final FilesToRunProvider ijar;
  private final ImmutableListMultimap<String, String> compatibleJavacOptions;
  private final ImmutableList<String> javacOptions;
  private final ImmutableList<String> jvmOptions;
  private final boolean javacSupportsWorkers;

  private JavaToolchainProvider(
      Label label,
      String sourceVersion,
      String targetVersion,
      NestedSet<Artifact> bootclasspath,
      NestedSet<Artifact> extclasspath,
      String encoding,
      Artifact javac,
      NestedSet<Artifact> tools,
      FilesToRunProvider javaBuilder,
      @Nullable Artifact headerCompiler,
      boolean forciblyDisableHeaderCompilation,
      Artifact singleJar,
      @Nullable Artifact oneVersion,
      @Nullable Artifact oneVersionWhitelist,
      Artifact genClass,
      @Nullable Artifact resourceJarBuilder,
      @Nullable Artifact timezoneData,
      FilesToRunProvider ijar,
      ImmutableListMultimap<String, String> compatibleJavacOptions,
      ImmutableList<String> javacOptions,
      ImmutableList<String> jvmOptions,
      boolean javacSupportsWorkers) {
    super(ImmutableMap.of(), Location.BUILTIN);

    this.label = label;
    this.sourceVersion = sourceVersion;
    this.targetVersion = targetVersion;
    this.bootclasspath = bootclasspath;
    this.extclasspath = extclasspath;
    this.encoding = encoding;
    this.javac = javac;
    this.tools = tools;
    this.javaBuilder = javaBuilder;
    this.headerCompiler = headerCompiler;
    this.forciblyDisableHeaderCompilation = forciblyDisableHeaderCompilation;
    this.singleJar = singleJar;
    this.oneVersion = oneVersion;
    this.oneVersionWhitelist = oneVersionWhitelist;
    this.genClass = genClass;
    this.resourceJarBuilder = resourceJarBuilder;
    this.timezoneData = timezoneData;
    this.ijar = ijar;
    this.compatibleJavacOptions = compatibleJavacOptions;
    this.javacOptions = javacOptions;
    this.jvmOptions = jvmOptions;
    this.javacSupportsWorkers = javacSupportsWorkers;
  }

  /** Returns the label for this {@code java_toolchain}. */
  public Label getToolchainLabel() {
    return label;
  }

  /** @return the input Java language level */
  public String getSourceVersion() {
    return sourceVersion;
  }

  /** @return the target Java language level */
  public String getTargetVersion() {
    return targetVersion;
  }

  /** @return the target Java bootclasspath */
  public NestedSet<Artifact> getBootclasspath() {
    return bootclasspath;
  }

  /** @return the target Java extclasspath */
  public NestedSet<Artifact> getExtclasspath() {
    return extclasspath;
  }

  /** @return the encoding for Java source files */
  public String getEncoding() {
    return encoding;
  }

  /** Returns the {@link Artifact} of the javac jar */
  public Artifact getJavac() {
    return javac;
  }

  /** Returns the {@link Artifact}s of compilation tools. */
  public NestedSet<Artifact> getTools() {
    return tools;
  }

  /** Returns the {@link FilesToRunProvider} of JavaBuilder */
  public FilesToRunProvider getJavaBuilder() {
    return javaBuilder;
  }

  /** @return the {@link Artifact} of the Header Compiler deploy jar */
  @Nullable
  public Artifact getHeaderCompiler() {
    return headerCompiler;
  }

  /**
   * Returns {@code true} if header compilation should be forcibly disabled, overriding
   * --java_header_compilation.
   */
  public boolean getForciblyDisableHeaderCompilation() {
    return forciblyDisableHeaderCompilation;
  }

  /** Returns the {@link Artifact} of the SingleJar deploy jar */
  public Artifact getSingleJar() {
    return singleJar;
  }

  /**
   * Return the {@link Artifact} of the binary that enforces one-version compliance of java
   * binaries.
   */
  @Nullable
  public Artifact getOneVersionBinary() {
    return oneVersion;
  }

  /** Return the {@link Artifact} of the whitelist used by the one-version compliance checker. */
  @Nullable
  public Artifact getOneVersionWhitelist() {
    return oneVersionWhitelist;
  }

  /** Returns the {@link Artifact} of the GenClass deploy jar */
  public Artifact getGenClass() {
    return genClass;
  }

  @Nullable
  public Artifact getResourceJarBuilder() {
    return resourceJarBuilder;
  }

  /**
   * Returns the {@link Artifact} of the latest timezone data resource jar that can be loaded by
   * Java 8 binaries.
   */
  @Nullable
  public Artifact getTimezoneData() {
    return timezoneData;
  }

  /** Returns the ijar executable */
  public FilesToRunProvider getIjar() {
    return ijar;
  }

  ImmutableListMultimap<String, String> getCompatibleJavacOptions() {
    return compatibleJavacOptions;
  }

  /** @return the map of target environment-specific javacopts. */
  public ImmutableList<String> getCompatibleJavacOptions(String key) {
    return getCompatibleJavacOptions().get(key);
  }

  /** @return the list of default options for the java compiler */
  public ImmutableList<String> getJavacOptions() {
    return javacOptions;
  }

  /**
   * @return the list of default options for the JVM running the java compiler and associated tools.
   */
  public ImmutableList<String> getJvmOptions() {
    return jvmOptions;
  }

  /** @return whether JavaBuilders supports running as a persistent worker or not */
  public boolean getJavacSupportsWorkers() {
    return javacSupportsWorkers;
  }
}
