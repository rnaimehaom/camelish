package cool;

import com.im.chemaxon.camel.db.DefaultJChemSearcher;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author timbo
 */
public class DrugBankSearcherTest extends CamelSpringTestSupport {
    
    private String url = "http://localhost:8880/chemsearch/drugbank";

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
    }

    @Test
    public void testSimpleSearch() {
        Object result = template.requestBodyAndHeader(
                url, "C1=CCCNC1",
                DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, "t:s");
        
        assert result != null;
        String str = context.getTypeConverter().convertTo(String.class, result);
        // not sure how to test this without a specific test set.
        // for now we just assume we receive some data and don't check the details
        assert str.length() > 100;
    }
    
    @Test
    public void testShouldBeNoResults() {
        // hope we can assume there will be no results for Uranium-1
        Object result = template.requestBodyAndHeader(
                url, "[1U]",
                DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, "t:d");
        
        assert result != null;
        String str = context.getTypeConverter().convertTo(String.class, result);
        assert str.length() == 0;
    }

}
