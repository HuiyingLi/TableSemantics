package edu.cmu.lti.huiying.ir.rangedsearch;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

public final class TableAnalyzer extends StopwordAnalyzerBase{

	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
	    final StandardTokenizer src = new StandardTokenizer(getVersion(), reader);
	    //src.setMaxTokenLength(maxTokenLength);
	    TokenStream tok = new StandardFilter(getVersion(), src);
	    tok = new LowerCaseFilter(getVersion(), tok);
	    tok = new StopFilter(getVersion(), tok, stopwords);
	    return new TokenStreamComponents(src, tok) {
	      @Override
	      protected void setReader(final Reader reader) throws IOException {
	        //src.setMaxTokenLength(StandardAnalyzer.this.maxTokenLength);
	        super.setReader(reader);
	      }
	    };
	  }

}
