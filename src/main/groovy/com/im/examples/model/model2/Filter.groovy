package com.im.examples.model.model2

import groovy.transform.Canonical

/** Includes the filter implementation so that it can be re-executed at a later stage
 */
@Canonical
class Filter {
    Object uuid
    Date created
    String owner
    List<Object> ids
    Closure impl
}

