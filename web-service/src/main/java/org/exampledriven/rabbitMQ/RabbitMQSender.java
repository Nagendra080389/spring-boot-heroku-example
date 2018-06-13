package org.exampledriven.rabbitMQ;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.ContentVersion;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.exampledriven.SpringBootHerokuExampleApplication;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void merge(BigOpertaion bigOpertaion) throws ConnectionException, IOException, DocumentException {


        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(bigOpertaion.getAccessToken());
        if (bigOpertaion.isUseSoap()) {
            config.setServiceEndpoint(bigOpertaion.getInstanceURL() + "/services/Soap/c/40.0");
        } else {
            config.setServiceEndpoint(bigOpertaion.getInstanceURL() + "/services/Soap/T/40.0");
        }


        List<File> inputFiles = new ArrayList<File>();


        String[] split = bigOpertaion.getFileIds().split(",");
        String parentId = split[split.length-1];
        StringBuilder buff = new StringBuilder();
        String sep = "";
        for (String str : split) {
            if(str != parentId) {
                buff.append(sep);
                buff.append("'"+str+"'");
                sep = ",";
            }
        }
        String queryIds = buff.toString();

        EnterpriseConnection enterpriseConnection = Connector.newConnection(config);

        QueryResult queryResults = enterpriseConnection.query(
                "Select Id,VersionData from ContentVersion where Id IN (Select LatestPublishedVersionId from ContentDocument where Id IN ("
                        + queryIds + "))");


        boolean done = false;

        if (queryResults.getSize() > 0) {
            while (!done) {
                for (SObject sObject : queryResults.getRecords()) {
                    ContentVersion contentData = (ContentVersion) sObject;
                    File tempFile = File.createTempFile("test_", ".pdf", null);
                    try (OutputStream os = Files.newOutputStream(Paths.get(tempFile.toURI()))) {
                        os.write(contentData.getVersionData());
                    }
                    inputFiles.add(tempFile);
                }
                if (queryResults.isDone()) {
                    done = true;
                }else {
                    queryResults = enterpriseConnection.queryMore(queryResults.getQueryLocator());
                }

            }
        }

        Document PDFCombineUsingJava = new Document();
        PdfSmartCopy copy = new PdfSmartCopy(PDFCombineUsingJava, new FileOutputStream("CombinedPDFDocument.pdf"));
        PDFCombineUsingJava.open();
        int number_of_pages = 0;
        inputFiles.parallelStream().forEachOrdered(inputFile -> {
            try {
                createFiles(inputFile, number_of_pages, copy);
            } catch (IOException | BadPdfFormatException e) {
                e.printStackTrace();
            }
        });

        PDFCombineUsingJava.close();
        copy.close();


        File mergedFile = new File("CombinedPDFDocument" + ".pdf");
        mergedFile.createNewFile();

        bigOpertaion.setFileToBeMerged(mergedFile);


        rabbitTemplate.convertAndSend(SpringBootHerokuExampleApplication.PDF_MERGE_QUEUE, bigOpertaion);
    }

    private void createFiles(File inputFile, int number_of_pages, PdfSmartCopy copy) throws IOException, BadPdfFormatException {
        PdfReader ReadInputPDF = new PdfReader(inputFile.toString());
        number_of_pages = ReadInputPDF.getNumberOfPages();
        for (int page = 0; page < number_of_pages; ) {
            copy.addPage(copy.getImportedPage(ReadInputPDF, ++page));
        }
        copy.freeReader(ReadInputPDF);
        ReadInputPDF.close();
    }

}
