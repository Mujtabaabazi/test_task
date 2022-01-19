/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mujtaba.processors.sample;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.commons.io.FileUtils;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.RuntimeErrorException;

@Tags({"example"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class MyProcessor extends AbstractProcessor {

    public static final PropertyDescriptor MY_PROPERTY = new PropertyDescriptor
            .Builder().name("MY_PROPERTY")
            .displayName("My property")
            .description("Example Property")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor DIRECTORY = new PropertyDescriptor.Builder()
            .name("Input Directory")
            .description("The input directory from which to pull files")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor OUTPUT_DIRECTORY = new PropertyDescriptor.Builder()
            .name("Output Directory")
            .description("The output directory where files will be saved")
            .required(true)
            .addValidator(StandardValidators.createDirectoryExistsValidator(true, false))
            .build();


    public static final Relationship MY_RELATIONSHIP = new Relationship.Builder()
            .name("MY_RELATIONSHIP")
            .description("Example relationship")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        descriptors = new ArrayList<>();
        descriptors.add(MY_PROPERTY);
        descriptors.add(DIRECTORY);
        descriptors.add(OUTPUT_DIRECTORY);
        descriptors = Collections.unmodifiableList(descriptors);

        relationships = new HashSet<>();
        relationships.add(MY_RELATIONSHIP);
        relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            return;
        }
        
        
        
        final File directory = new File(context.getProperty(DIRECTORY).evaluateAttributeExpressions().getValue());
        //File file = new File(input);
	    try {
	        Document doc = Jsoup.parse(directory, "UTF-8");
	        Elements allparagraphs  = doc.select("p.flush-paragraph-2");
	        
	        List<Element> paragraphsList = new ArrayList<Element>();
	        for (int i=0;i<allparagraphs.size(); i++)  {
	        	Element paragraph = allparagraphs.get(i);
	        	paragraphsList.add(paragraph);
	        }
	        		  
	        for(int k=0; k<paragraphsList.size();k++) {
	        	Element para = paragraphsList.get(k);
	        	System.out.println("para" + para);
	        	Elements siblings = para.nextElementSiblings();
	        	//System.out.println("para all siblings " + siblings);
	        	String h2Text = para.html();
	        
		        List<Element> elementsBetween = new ArrayList<Element>();
		        inner: for(int j=0;j<siblings.size(); j++){
		        	//System.out.println("entered for");
		            Element sibling = siblings.get(j);
		        	//System.out.println(sibling);
		            if(!"flush-paragraph-2".equals(sibling.className())){
			        	//System.out.println("entered if");
		                elementsBetween.add(sibling);
		                
		            } else if (elementsBetween.isEmpty()) {
			                processElementsBetween(context, h2Text, elementsBetween);
		            		//System.out.println("first");
		            		//System.out.println(h2Text);
		            		break inner;
		                
		            } else {
		                	processElementsBetween(context, h2Text, elementsBetween);
	            			//System.out.println("second");
		            		//System.out.println(h2Text);
		            		elementsBetween.clear();
		            		//System.out.println(elementsBetween);
		            		break inner;
		            		//h2Text = sibling.html();
		            }	
	                processElementsBetween(context, h2Text, elementsBetween);  
		        }    			        
	    }
	        
	     session.transfer(flowFile, MY_RELATIONSHIP);
	     
	        
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        throw new RuntimeException(e);
	        
	    }
	    
	    
	    
	}
	
	private void processElementsBetween(final ProcessContext context, String h2Text, List<Element> elementsBetween) throws IOException {
			
        	final File outputDirectory = new File(context.getProperty(OUTPUT_DIRECTORY).evaluateAttributeExpressions().getValue());

	    	String fileName = h2Text.substring(8,13);
	    	
		    File newHtmlFile = new File(outputDirectory.getAbsolutePath() + "\\" + fileName+".html");
		    StringBuffer htmlString = new StringBuffer("");
		    htmlString.append("<html><body>");
		    htmlString.append(h2Text);
		      for (Element element : elementsBetween) {
		          htmlString.append(element.toString());
		              }
		      htmlString.append("</body></html>");
		      FileUtils.writeStringToFile(newHtmlFile, htmlString.toString());
		    }
	
	public static File findFile(File outputDirectory, String fileName) {
	      File file = new File(outputDirectory, fileName);
	      File parent = file.getParentFile();
	      if (parent != null)
	          parent.mkdirs();
	      return file;
	  }

    }

