package com.im.bioassay.curvefit

import spock.lang.Shared
import spock.lang.Specification


class FoutPLFitterSpecification extends Specification {
    
    @Shared
    def testConcs = [
        [1, 10, 100, 1000, 10000, 100000] as double[],
        [1, 10, 100, 1000, 10000, 100000] as double[],
        [1, 10, 100, 1000, 10000, 100000] as double[],
        [1, 1, 10, 10, 100, 100, 1000, 1000, 10000, 10000, 100000, 100000] as double[]
      ]
    @Shared  
    testInhibitions = [
        [5, 9, 25, 70, 90, 95] as double[],
        [3, 6, 11, 25, 85, 96] as double[],
        [3, 6, 11, 25, 85, 96].reverse() as double[] ,     // high to low doesn't work
        [3, 2, 6, 6, 11, 12, 25, 23, 85, 88, 96, 101] as double[],
      ]


    def "all params varying"() {

      setup:
      def fitter = new FourPLFitter()

      expect:
      def ic50 = fitter.calcBestModel(concValues, inhibitionValues)
      ic50.inflection > inflections - 2 && ic50.inflection < inflections + 2
      ic50.slope > slopes - 0.1 && ic50.slope < slopes + 0.1

      where:
      concValues << testConcs
      inhibitionValues << testInhibitions
      inflections << [
        370.1,
        2585.9,
        38.6,
        2749
      ]

      slopes << [
        0.94,
        1.4,
        -1.4,
        1.4
      ]
    }

def "top bottom fixed"() {

      setup:
      def fitter = new FourPLFitter(0, 100, null, null)

      expect:
      def ic50 = fitter.calcBestModel(concValues, inhibitionValues)
      ic50.bottom == 0d
      ic50.top == 100d
      ic50.inflection > inflections - 2 && ic50.inflection < inflections + 2
      ic50.slope > slopes - 0.1 && ic50.slope < slopes + 0.1

      where:
      concValues << testConcs
      inhibitionValues << testInhibitions
      inflections << [
        367,
        2400,
        41.6,
        2377
      ]

      slopes << [
        0.71,
        1.1,
        -1.1,
        1.1
      ]
    }

}
