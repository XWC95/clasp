package com.vea.clasp.plugin.cache;

import com.vea.clasp.plugin.api.util.RelativeDirectoryProvider;

import java.io.File;

public interface RelativeDirectoryProviderFactory {

    RelativeDirectoryProvider newProvider(File root);
}
