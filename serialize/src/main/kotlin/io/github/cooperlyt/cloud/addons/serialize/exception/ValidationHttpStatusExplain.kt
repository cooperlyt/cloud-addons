package io.github.cooperlyt.cloud.addons.serialize.exception

class ValidationHttpStatusExplain(
    val fields: Map<String, String>,
    path: String
) : HttpStatusExplain(path,100001,"validation_fail")