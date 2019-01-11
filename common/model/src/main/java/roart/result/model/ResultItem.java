package roart.result.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "_class")  
	@JsonSubTypes({  
	    @Type(value = ResultItemTable.class, name = "roart.model.ResultItemTable"),  
	    @Type(value = ResultItemBytes.class, name = "roart.model.ResultItemStream"),  
	    @Type(value = ResultItemText.class, name = "roart.model.ResultItemText") })  
public abstract class ResultItem {
}
