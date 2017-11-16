package com.pgy.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.MultipartBuilder
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.Response
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.json.JSONObject

import java.util.concurrent.TimeUnit

public class PgyUploadPlugin implements Plugin<Project> {

    private final String API_END_POINT = "http://www.pgyer.com/apiv1"

    void apply(Project project) {
        project.extensions.create("pgy", PgyUploadExtension)

        project.afterEvaluate {
            PgyUploadExtension pgyExt = project.extensions.findByName("pgy") as PgyUploadExtension

            if (!project.plugins.hasPlugin("com.android.application")) {
                throw new RuntimeException("PgyUploadPlugin can only be applied for android application module.")
            }

            project.android.applicationVariants.each { variant ->
                injectPgyTask(project, variant, pgyExt)
            }
        }
    }

    void injectPgyTask(Project project, ApplicationVariant variant, PgyUploadExtension config) {
        String name = variant.name
        String newName = name.substring(0, 1).toUpperCase() + name.substring(1)
        def pgyTask = project.tasks.create(name: "pgy${newName}") << {
            // 获取要上传的APK
            File apk = variant.outputs.last().outputFile
            upload(apk, config)
        }

        pgyTask.group = "pgy"

        pgyTask.dependsOn project.tasks.getByPath("assemble${newName}")
    }


    void upload(File apkFile, PgyUploadExtension config) {
        JSONObject resultJson = httpPost(apkFile, config)
        errorHandling(resultJson)
        println "APK upload  result: ${resultJson.toString()}"

    }

    private void errorHandling(JSONObject json) {
        if (!json.toString().isEmpty() && json.getInt("code") == 0) {
            return
        } else {
            throw new GradleException("up load fail: result=${json.toString()}")
        }
    }

    private String getEndPoint(PgyUploadExtension config) {
        String uKey = config.uKey
        String _api_key = config._api_key
        if (uKey == null || _api_key == null) {
            throw new GradleException("uKey or apiKey is missing")
        }
        String endPoint = API_END_POINT + "/app/upload"
        return endPoint
    }

    private JSONObject httpPost(File apkFile, PgyUploadExtension config) {
        OkHttpClient client = new OkHttpClient()
        client.setConnectTimeout(10, TimeUnit.SECONDS)
        client.setReadTimeout(60, TimeUnit.SECONDS)

        MultipartBuilder multipartBuilder = new MultipartBuilder()
                .type(MultipartBuilder.FORM)


        multipartBuilder.addFormDataPart("_api_key", config._api_key)
        multipartBuilder.addFormDataPart("uKey", config.uKey)
        multipartBuilder.addFormDataPart("updateDescription", config.updateDescription)

        multipartBuilder.addFormDataPart("file",
                apkFile.getName(),
                RequestBody.create(
                        MediaType.parse("application/vnd.android.package-archive"),
                        apkFile)
        )


        Request request = new Request.Builder().url(getEndPoint(config)).
                post(multipartBuilder.build()).
                build()

        Response response = client.newCall(request).execute()

        if (response == null || response.body() == null) return null
        InputStream is = response.body().byteStream()
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        JSONObject json = new JSONObject(reader.readLine())
        is.close()

        return json
    }
}
