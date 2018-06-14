package org.exampledriven.rabbitMQ;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.sforce.soap.enterprise.*;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.sobject.ContentVersion;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.apache.commons.io.IOUtils;
import org.exampledriven.SpringBootHerokuExampleApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class RabbitMQListener {

    Logger LOGGER = LoggerFactory.getLogger(RabbitMQListener.class);
    static EnterpriseConnection connection;
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

    @RabbitListener(queues = SpringBootHerokuExampleApplication.PDF_MERGE_QUEUE)
    public void mergeProcess(BigOpertaion bigOpertaion) {
        mergeanduploadPDF(bigOpertaion.getFileIds(), bigOpertaion.getAccessToken(), bigOpertaion.getInstanceURL(), bigOpertaion.isUseSoap(), bigOpertaion.getListOfByteArrays());
    }

    @RabbitListener(queues = SpringBootHerokuExampleApplication.PDF_SPLIT_QUEUE)
    public void splitProcess(BigOpertaion bigOpertaion) {

    }

    public void mergeanduploadPDF(String file1Ids, String accessToken, String instanceURL, boolean useSoap, List<byte[]> listOfByteArrays) {

        System.out.println("Querying for the mail request...");

        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(accessToken);
        if (useSoap) {
            config.setServiceEndpoint(instanceURL + "/services/Soap/c/40.0");
        } else {
            config.setServiceEndpoint(instanceURL + "/services/Soap/T/40.0");
        }

        String[] split = file1Ids.split(",");
        String parentId = split[split.length - 1];

        try {
            connection = Connector.newConnection(config);

            Document PDFCombineUsingJava = new Document();
            PdfSmartCopy copy = new PdfSmartCopy(PDFCombineUsingJava, new FileOutputStream("CombinedPDFDocument.pdf"));
            PDFCombineUsingJava.open();
            int number_of_pages = 0;
            listOfByteArrays.parallelStream().forEachOrdered(eachbyteArray -> {
                try {
                    createFiles(eachbyteArray, number_of_pages, copy);
                } catch (IOException | BadPdfFormatException e) {
                    e.printStackTrace();
                }
            });

            PDFCombineUsingJava.close();
            copy.close();
            File mergedFile = new File("CombinedPDFDocument" + ".pdf");
            mergedFile.createNewFile();

            LOGGER.info("Creating ContentVersion record...");
            ContentVersion[] record = new ContentVersion[1];
            ContentVersion mergedContentData = new ContentVersion();
            mergedContentData.setVersionData(readFromFile(mergedFile.getName()));
            mergedContentData.setFirstPublishLocationId(parentId);
            mergedContentData.setTitle("Merged Document");
            mergedContentData.setPathOnClient("/CombinedPDFDocument.pdf");

            record[0] = mergedContentData;


            // create the records in Salesforce.com
            SaveResult[] saveResults = connection.create(record);

            // check the returned results for any errors
            for (int i = 0; i < saveResults.length; i++) {
                if (saveResults[i].isSuccess()) {
                    System.out.println(i + ". Successfully created record - Id: " + saveResults[i].getId());
                } else {
                    Error[] errors = saveResults[i].getErrors();
                    for (int j = 0; j < errors.length; j++) {
                        System.out.println("ERROR creating record: " + errors[j].getMessage());
                    }
                }
            }
        } catch (ConnectionException | IOException | DocumentException e) {
            e.printStackTrace();
        }

    }

    private void createFiles(byte[] eachByteArray, int number_of_pages, PdfSmartCopy copy) throws IOException, BadPdfFormatException {
        PdfReader ReadInputPDF = new PdfReader(eachByteArray);
        number_of_pages = ReadInputPDF.getNumberOfPages();
        for (int page = 0; page < number_of_pages; ) {
            copy.addPage(copy.getImportedPage(ReadInputPDF, ++page));
        }
        copy.freeReader(ReadInputPDF);
        ReadInputPDF.close();
    }

    public static byte[] readFromFile(String fileName) throws IOException {
        byte[] buf = new byte[8192];
        try (InputStream is = Files.newInputStream(Paths.get(fileName))) {
            int len = is.read(buf);
            if (len < buf.length) {
                return Arrays.copyOf(buf, len);
            }
            try (ByteArrayOutputStream os = new ByteArrayOutputStream(16384)) {
                while (len != -1) {
                    os.write(buf, 0, len);
                    len = is.read(buf);
                }
                return os.toByteArray();
            }
        }
    }

    public void splitanduploadPDF(String documentId, String parentId, String accessToken, String instanceURL, boolean useSoap) {

        try {

            System.out.println("Querying for the mail request...");

            ConnectorConfig config = new ConnectorConfig();
            config.setSessionId(accessToken);
            if (useSoap) {
                config.setServiceEndpoint(instanceURL + "/services/Soap/c/40.0");
            } else {
                config.setServiceEndpoint(instanceURL + "/services/Soap/T/40.0");
            }
            connection = Connector.newConnection(config);

            // query for the attachment data
            QueryResult queryResults = connection.query(
                    "Select Id,VersionData from ContentVersion where Id IN(Select LatestPublishedVersionId from ContentDocument where Id = '"
                            + documentId + "')");
            System.out.println("in here.." + queryResults.getSize());
            File tempFile = File.createTempFile("test_", ".pdf", null);
            for (int i = 0; i < queryResults.getSize(); i++) {
                ContentVersion contentData = (ContentVersion) queryResults.getRecords()[i];
                System.out.println(i + "..file size.." + contentData.getVersionData().length + "    "
                        + contentData.getVersionData());
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(contentData.getVersionData());
                }
            }
            PdfReader Split_PDF_Document = new PdfReader(tempFile.toString());
            Document document;
            document = new Document();
            String FileName = "File" + 1 + ".pdf";
            PdfSmartCopy copy = new PdfSmartCopy(document, new FileOutputStream(FileName));
            document.open();
            copy.addPage(copy.getImportedPage(Split_PDF_Document, 1));
            copy.close();
            document.close();
            Split_PDF_Document.close();
            File splitFile = new File(FileName);
            splitFile.createNewFile();

            System.out.println("Creating ContentVersion record...");
            ContentVersion[] record = new ContentVersion[1];
            ContentVersion splitContentData = new ContentVersion();

            InputStream is = new FileInputStream(splitFile);
            splitContentData.setVersionData(IOUtils.toByteArray(is));

            is.close();
            splitContentData.setFirstPublishLocationId(parentId);
            splitContentData.setTitle("Split Document");
            splitContentData.setPathOnClient(FileName);

            record[0] = splitContentData;

            // create the records in Salesforce.com
            SaveResult[] saveResults = connection.create(record);

            // check the returned results for any errors
            for (int i = 0; i < saveResults.length; i++) {
                if (saveResults[i].isSuccess()) {
                    System.out.println(i + ". Successfully created record - Id: " + saveResults[i].getId());
                } else {
                    Error[] errors = saveResults[i].getErrors();
                    for (int j = 0; j < errors.length; j++) {
                        System.out.println("ERROR creating record: " + errors[j].getMessage());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
