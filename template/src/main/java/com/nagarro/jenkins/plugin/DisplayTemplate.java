package com.nagarro.jenkins.plugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import com.nagarro.jenkins.plugin.exceptions.JenkinsSeedJobFailException;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJobs;

public class DisplayTemplate {
	
	private final static Logger LOGGER = Logger.getLogger(DisplayTemplate.class);
	
	private static final String ERR_MIME_TYPE= "File contents can not be displayed in the console since the mime-type property says that it's not a kind of a text file.";
    /**
     * To get the job defined in jenkins.xml from SVN
     * @param jenkinsXmlAndRepoValuesMap
     * @param args
     * @return
     * @throws JenkinsSeedJobFailException 
     */
	public JenkinsJobs getJenkinsJob(Map<String, String> jenkinsXmlAndRepoValuesMap, String... args) throws JenkinsSeedJobFailException{
		System.out.println("svn url : " + args[0]+"Username :"+args[1]+"Password :"+args[2]+"filename: "+args[3]);
		FileBuilder fileBuilder = new FileBuilder();
		JenkinsJobs jenkinsJobs = null;
		
		String url = args[0]; // String url = "http://svn.nagarro.local:8080/svn/DevOps/poc/sampleTestProject";
		String name = args[1];
		String password = args[2];
		String fileName = args[3];//jenkins.xml retrieved from jenkins.properties file
		LOGGER.info("Username :"+name+"Password :"+password+"filename: "+fileName);
		/*
		 * Initializes the library (it must be done before ever using the
		 * library itself)
		 */
		initLibrary();

		SVNRepository repository = null;
		try {
			/*
			 * Creates an instance of SVNRepository to work with the repository.
			 * All user's requests to the repository are relative to the
			 * repository location used to create this SVNRepository.
			 * SVNURL is a wrapper for URL strings that refer to repository locations.
			 */
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
		} catch (SVNException svne) {
			LOGGER.info("error while creating an SVNRepository for the location " + url);
			throw new JenkinsSeedJobFailException(svne);
		}

		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, "ihzj1789!");//Sushil - hardcoded password passed in old code. Bad practice
		repository.setAuthenticationManager(authManager);
		/*
		 * This Map will be used to get the file properties. Each Map key is a
		 * property name and the value associated with the key is the property
		 * value.
		 */
		SVNProperties fileProperties = new SVNProperties();
		ByteArrayOutputStream fileContent = new ByteArrayOutputStream();

		try {
			/*
			 * Checks up if the specified path really corresponds to a file. If
			 * doesn't the program exits. SVNNodeKind is that one who says what is
			 * located at a path in a revision. -1 means the latest revision.
			 */
			SVNNodeKind nodeKind = repository.checkPath(fileName, -1);
			System.out.println("nodeKind: " + nodeKind);
			if (nodeKind == SVNNodeKind.NONE) {
				LOGGER.error("There is no entry at '" + url + "'.");
				return jenkinsJobs;
			} else if (nodeKind == SVNNodeKind.DIR) {
				LOGGER.error("The entry at '" + url
						+ "' is a directory while a file was expected.");
			}
			/*
			 * Gets the contents and properties of the file located at filePath
			 * in the repository at the latest revision (which is meant by a
			 * negative revision number).
			 */
			repository.getFile(fileName, -1, fileProperties, fileContent);

		} catch (SVNException svne) {
			LOGGER.error("error while fetching the file contents and properties: ");
			throw new JenkinsSeedJobFailException(svne);
		}

		/*
		 * Here the SVNProperty class is used to get the value of the
		 * svn:mime-type property (if any). SVNProperty is used to facilitate
		 * the work with versioned properties.
		 */
		String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);

		/*for (Object c : fileProperties.values()) {
        	System.out.println("property1::::::: ++++++++++++++++" + c);
        }
		 */
		jenkinsXmlAndRepoValuesMap.put("lastAuthor", fileProperties.getStringValue(SVNProperty.LAST_AUTHOR));
		jenkinsXmlAndRepoValuesMap.put("committedRevision", fileProperties.getStringValue(SVNProperty.COMMITTED_REVISION));
		/*
		 * SVNProperty.isTextMimeType(..) method checks up the value of the mime-type
		 * file property and says if the file is a text (true) or not (false).
		 */
		boolean isTextType = SVNProperty.isTextMimeType(mimeType);

		/*
		 * Displays file properties.
		 */
		/* while (iterator.hasNext()) {
            String propertyName = (String) iterator.next();
            String propertyValue = fileProperties.getStringValue(propertyName);
            System.out.println("File property: " + propertyName + "="
                    + propertyValue);
        }*/
		/*
		 * Displays the file contents in the console if the file is a text.
		 */
		if (isTextType) {
			System.out.println("File contents:\n");
			try {
				fileContent.writeTo(System.out);
			} catch (IOException ioe) {
				throw new JenkinsSeedJobFailException(ioe);
			}
			System.out.println("File Size:"+fileContent.size()+"\t"+"Empty check:"+fileContent.toString().isEmpty()+"Length check after trim"+fileContent.toString().trim().length());
			if( !fileContent.toString().isEmpty() || fileContent.toString().length() == 0){
				if(fileContent.toString().trim().length() > 0)
					jenkinsJobs = fileBuilder.jenkinsxmlToJenkinsJob(fileContent.toString());// converting jenkin jobs to JenkinsJob pojo
				LOGGER.info("Sushil - For Jenkins xml file  -----------&&&&&&&    "+fileContent.toString());
			}
		} else {
			LOGGER.info(ERR_MIME_TYPE);
		}   
		
		
		LOGGER.info("Sushil - For Jenkins xml file  -----------&&&&&&&    "+fileContent.toString());
		
		return jenkinsJobs;
	}

    private static void initLibrary() {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }
}
