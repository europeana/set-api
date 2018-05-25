<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@include file="header.html"%>
<% 
String pageDescription="JSONLD templates for creation of user set objects";
String withType = request.getParameter("withType");
boolean hasType = withType != null;		
%>	
<%@include file="description.jspf"%>

<p>
The following properties are optional in all user sets:
<b>context, type, creator, created.</b> 
</p>
					<ul id="toc">
						<li><a href="#tag_userSet">Create User Set</a></li>
					</ul>

<h3 id="tag_userSet">Create User Set</h3>
The json-ld serialization available in the following box is a valid input to be used for the creation of simple <b>sets</b>.
&nbsp;&nbsp;&nbsp; <a href="#top">top</a> 
<textarea rows="18" cols="120" name="jsonldtag">
{
  "type": "Collection",
  "title": {
     "en": "Sportswear"
  },
  "description": {
     "en": "From tennis ensemble to golf uniforms, browse Europeana Fashion wide collection of historical sportswear and activewear designs!"
  },
  "items": [
    "http://data.europeana.eu/item/000000/1",
    "http://data.europeana.eu/item/000000/2",
    "http://data.europeana.eu/item/000000/3",
    "http://data.europeana.eu/item/000000/4",
    "http://data.europeana.eu/item/000000/5",
    "http://data.europeana.eu/item/000000/6",
    "http://data.europeana.eu/item/000000/7",
    "http://data.europeana.eu/item/000000/8",
    "http://data.europeana.eu/item/000000/9",
    "http://data.europeana.eu/item/000000/10",
    "http://data.europeana.eu/item/000000/11",
    "http://data.europeana.eu/item/000000/12",
    "http://data.europeana.eu/item/000000/13",
    "http://data.europeana.eu/item/000000/14",
    "http://data.europeana.eu/item/000000/15",
    "http://data.europeana.eu/item/000000/16",
    "http://data.europeana.eu/item/000000/17",
    "http://data.europeana.eu/item/000000/18",
    "http://data.europeana.eu/item/000000/19",
    "http://data.europeana.eu/item/000000/20",
    "http://data.europeana.eu/item/000000/21",
    "http://data.europeana.eu/item/000000/22",
    "http://data.europeana.eu/item/000000/23",
    "http://data.europeana.eu/item/000000/24",
    "http://data.europeana.eu/item/000000/25",
    "http://data.europeana.eu/item/000000/26",
    "http://data.europeana.eu/item/000000/27",
    "http://data.europeana.eu/item/000000/28",
    "http://data.europeana.eu/item/000000/29",
    "http://data.europeana.eu/item/000000/30",
    "http://data.europeana.eu/item/000000/31",
    "http://data.europeana.eu/item/000000/32",
    "http://data.europeana.eu/item/000000/33",
    "http://data.europeana.eu/item/000000/34",
    "http://data.europeana.eu/item/000000/35",
    "http://data.europeana.eu/item/000000/36",
    "http://data.europeana.eu/item/000000/37",
    "http://data.europeana.eu/item/000000/38",
    "http://data.europeana.eu/item/000000/39",
    "http://data.europeana.eu/item/000000/40",
    "http://data.europeana.eu/item/000000/41",
    "http://data.europeana.eu/item/000000/42",
    "http://data.europeana.eu/item/000000/43",
    "http://data.europeana.eu/item/000000/44",
    "http://data.europeana.eu/item/000000/45",
    "http://data.europeana.eu/item/000000/46",
    "http://data.europeana.eu/item/000000/47",
    "http://data.europeana.eu/item/000000/48",
    "http://data.europeana.eu/item/000000/49",
    "http://data.europeana.eu/item/000000/50",
    "http://data.europeana.eu/item/000000/51",
    "http://data.europeana.eu/item/000000/52",
    "http://data.europeana.eu/item/000000/53",
    "http://data.europeana.eu/item/000000/54",
    "http://data.europeana.eu/item/000000/55",
    "http://data.europeana.eu/item/000000/56",
    "http://data.europeana.eu/item/000000/57",
    "http://data.europeana.eu/item/000000/58",
    "http://data.europeana.eu/item/000000/59",
    "http://data.europeana.eu/item/000000/60",
    "http://data.europeana.eu/item/000000/61",
    "http://data.europeana.eu/item/000000/62",
    "http://data.europeana.eu/item/000000/63",
    "http://data.europeana.eu/item/000000/64",
    "http://data.europeana.eu/item/000000/65",
    "http://data.europeana.eu/item/000000/66",
    "http://data.europeana.eu/item/000000/67",
    "http://data.europeana.eu/item/000000/68",
    "http://data.europeana.eu/item/000000/69",
    "http://data.europeana.eu/item/000000/70",
    "http://data.europeana.eu/item/000000/71",
    "http://data.europeana.eu/item/000000/72",
    "http://data.europeana.eu/item/000000/73",
    "http://data.europeana.eu/item/000000/74",
    "http://data.europeana.eu/item/000000/75",
    "http://data.europeana.eu/item/000000/76",
    "http://data.europeana.eu/item/000000/77",
    "http://data.europeana.eu/item/000000/78",
    "http://data.europeana.eu/item/000000/79",
    "http://data.europeana.eu/item/000000/80",
    "http://data.europeana.eu/item/000000/81",
    "http://data.europeana.eu/item/000000/82",
    "http://data.europeana.eu/item/000000/83",
    "http://data.europeana.eu/item/000000/84",
    "http://data.europeana.eu/item/000000/85",
    "http://data.europeana.eu/item/000000/86",
    "http://data.europeana.eu/item/000000/87",
    "http://data.europeana.eu/item/000000/88",
    "http://data.europeana.eu/item/000000/89",
    "http://data.europeana.eu/item/000000/90",
    "http://data.europeana.eu/item/000000/91",
    "http://data.europeana.eu/item/000000/92",
    "http://data.europeana.eu/item/000000/93",
    "http://data.europeana.eu/item/000000/94",
    "http://data.europeana.eu/item/000000/95",
    "http://data.europeana.eu/item/000000/96",
    "http://data.europeana.eu/item/000000/97",
    "http://data.europeana.eu/item/000000/98",
    "http://data.europeana.eu/item/000000/99",
    "http://data.europeana.eu/item/000000/100"
  ]
}
</textarea>
<br>

<br>			
<%@include file="footer.html"%>


