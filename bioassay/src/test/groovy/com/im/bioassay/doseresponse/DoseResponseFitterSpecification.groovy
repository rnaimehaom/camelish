package com.im.bioassay.doseresponse

import spock.lang.Specification


class DoseResponseFitterSpecification extends Specification {


    def "simple fitting"() {

        when:
        def result = DataUtils.createSingle()
        def fitter = new DoseResponseFitter()
        fitter.fit(result)

        then:
        result.fitModel != null
        result.fitModel.inflection != null
        result.fitModel.slope != null

    }
}
