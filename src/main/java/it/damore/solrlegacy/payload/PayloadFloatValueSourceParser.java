package it.damore.solrlegacy.payload;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.ConstValueSource;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.payloads.PayloadFunction;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.StrField;
import org.apache.solr.schema.TextField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.HashMap;
import java.util.Map;

public class PayloadFloatValueSourceParser extends ValueSourceParser {

    private Map<FieldType, PayloadDecoder> decoders = new HashMap<>();  // cache to avoid scanning token filters repeatedly, unnecessarily

    public void init(NamedList namedList) {
    }

    private static TInfo parseTerm(FunctionQParser fp) throws SyntaxError {
        TInfo tinfo = new TInfo();

        tinfo.indexedField = tinfo.field = fp.parseArg();
        tinfo.val = fp.parseArg();
        tinfo.indexedBytes = new BytesRefBuilder();

        FieldType ft = fp.getReq().getSchema().getFieldTypeNoEx(tinfo.field);
        if (ft == null) ft = new StrField();

        if (ft instanceof TextField) {
            // need to do analysis on the term
            String indexedVal = tinfo.val;
            Query q = ft.getFieldQuery(fp, fp.getReq().getSchema().getFieldOrNull(tinfo.field), tinfo.val);
            if (q instanceof TermQuery) {
                Term term = ((TermQuery)q).getTerm();
                tinfo.indexedField = term.field();
                indexedVal = term.text();
            }
            tinfo.indexedBytes.copyChars(indexedVal);
        } else {
            ft.readableToIndexed(tinfo.val, tinfo.indexedBytes);
        }

        return tinfo;
    }

    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
        // payload(field,value[,default, ['min|max|average|first']])
        //   defaults to "average" and 0.0 default value

        TInfo tinfo = parseTerm(fp); // would have made this parser a new separate class and registered it, but this handy method is private :/

        ValueSource defaultValueSource;
        if (fp.hasMoreArguments()) {
            defaultValueSource = fp.parseValueSource();
        } else {
            defaultValueSource = new ConstValueSource(0.0f);
        }

        PayloadFunction payloadFunction = null;
        String func = "average";
        if (fp.hasMoreArguments()) {
            func = fp.parseArg();
        }
        payloadFunction = PayloadUtils.getPayloadFunction(func);

        // Support func="first" by payloadFunction=null
        if(payloadFunction == null && !"first".equals(func)) {
            // not "first" (or average, min, or max)
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Invalid payload function: " + func);
        }

        IndexSchema schema = fp.getReq().getCore().getLatestSchema();
        PayloadDecoder decoder = getPayloadDecoder(tinfo.field, schema);

        if (decoder==null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No payload decoder found for field: " + tinfo.field);
        }

        return new FloatPayloadValueSource(
                tinfo.field,
                tinfo.val,
                tinfo.indexedField,
                tinfo.indexedBytes.get(),
                decoder,
                payloadFunction,
                defaultValueSource);
    }

    public PayloadDecoder getPayloadDecoder(String field, IndexSchema schema) {
        FieldType ft = schema.getFieldType(field);
        if (ft == null)
            return null;
        return decoders.computeIfAbsent(ft, f -> PayloadUtils.getPayloadDecoder(ft));
    }

    private static class TInfo {
        String field;
        String val;
        String indexedField;
        BytesRefBuilder indexedBytes;
    }

}
