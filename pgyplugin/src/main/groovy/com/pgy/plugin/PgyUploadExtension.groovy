package com.pgy.plugin

import org.gradle.api.Project

public class PgyUploadExtension {
    String _api_key
    String uKey
    String updateDescription

    PgyUploadExtension() {
        _api_key = ""
        uKey = ""
        updateDescription = ""
    }

    public static PgyUploadExtension getConfig(Project project) {
        PgyUploadExtension config =
                project.getExtensions().findByType(PgyUploadExtension.class)
        if (config == null) {
            config = new PgyUploadExtension()
        }
        return config
    }
}
