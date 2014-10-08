package com.im.chemaxon.examples.jchemsearch

import chemaxon.sss.search.JChemSearchOptions
import chemaxon.sss.SearchConstants;

JChemSearchOptions opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE)
println opts

opts.setStereoSearchType(SearchConstants.STEREO_IGNORE)
println opts

opts.setSearchType(SearchConstants.DUPLICATE)
println opts

opts.setStereoSearchType(SearchConstants.STEREO_IGNORE)
println opts

opts.setStereoModel(SearchConstants.STEREO_MODEL_LOCAL)
println opts

opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE)
opts.setOptions('sep=, t:d,exactStereoSearch:n')
println opts

opts.setSearchType(SearchConstants.FULL)
println opts


opts.setSearchType(SearchConstants.SIMILARITY)
println opts

opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE)
opts.setMaxResultCount(10)
println opts