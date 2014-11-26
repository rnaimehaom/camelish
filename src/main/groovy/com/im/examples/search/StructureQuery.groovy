package com.im.examples.search

import groovy.transform.Canonical

@Canonical
class StructureQuery implements Serializable {
    String query
    String searchOptions
}

