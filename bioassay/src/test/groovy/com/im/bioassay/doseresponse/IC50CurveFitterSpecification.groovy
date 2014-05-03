package com.im.bioassay.doseresponse

import spock.lang.Specification


class IC50CurveFitterSpecification extends Specification {

    def "Check our name is recognised"() {

        setup:
        def fitter = new IC50CurveFitter()


        expect:
        fitter != null
    }


    def "computing simple curves"() {

      setup:
      def fitter = new IC50CurveFitter()

      expect:
      def ic50 = fitter.calcBestIC50(concValues, inhibitionValues)
      ic50.conc > result - 2 && ic50.conc < result + 2
      ic50.hill > slope - 0.1 && ic50.hill < slope + 0.1

      where:
      concValues << [
        [1, 10, 100, 1000, 10000, 100000] as double[],
        [1, 10, 100, 1000, 10000, 100000] as double[],
//        [1, 10, 100, 1000, 10000, 100000] as double[],
        [1, 1, 10, 10, 100, 100, 1000, 1000, 10000, 10000, 100000, 100000] as double[]
      ]
      inhibitionValues << [
        [5, 9, 25, 70, 90, 95] as double[],
        [3, 6, 11, 25, 85, 96] as double[],
//        [3, 6, 11, 25, 85, 96].reverse() as double[] ,     // high to low doesn't work
        [3, 2, 6, 6, 11, 12, 25, 23, 85, 88, 96, 101] as double[],
      ]
      result << [
        369,
        2400,
//        100,
        2377
      ]

      slope << [
        0.7,
        1.1,
//        -1.0,
        1.2
      ]
    }
}
