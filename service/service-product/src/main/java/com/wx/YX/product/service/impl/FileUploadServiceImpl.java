package com.wx.YX.product.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.wx.YX.product.service.FileUploadService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    //从yml配置中获取
//    @Value("${aliyun.endpoint}")
//    private String endPoint;
//    @Value("${aliyun.keyid}")
//    private String accessKey;
//    @Value("${aliyun.keysecret}")
//    private String secreKey;
//    @Value("${aliyun.bucketname}")
//    private String bucketName;

    @Override
    public String fileUpload(MultipartFile file) throws Exception {
         // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        //String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
       // EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        //String bucketName = "examplebucket";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        //String objectName = "exampledir/exampleobject.txt";
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        String filePath= "D:\\localpath\\examplefile.txt";

        Properties properties = new Properties();
        // 设置config.ini文件路径
        String configFilePath = "/home/meng/文档/YX-Parent/service/service-product/src/main/resources/config.ini";

        // 读取配置文件
        FileInputStream input = new FileInputStream(configFilePath);
        properties.load(input);
        input.close();

        // 从配置文件中获取AK和SK
        String accessKeyId = properties.getProperty("accessKeyId");
        String accessKeySecret = properties.getProperty("accessKeySecret");

        String endPoint= properties.getProperty("endPoint");
        String bucketName= properties.getProperty("bucketName");
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);





        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endPoint, credentialsProvider);

        try {
            InputStream inputStream= file.getInputStream();
            String objectName = file.getOriginalFilename();
            String uuid= UUID.randomUUID().toString().replaceAll("-","");
            objectName=uuid+objectName;

            //对上传文件进行分组，根据当前年月日
            String currentDateTimem=new DateTime().toString("yyyy/MM/dd");
            objectName=currentDateTimem+"/"+objectName;

            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传文件。
            PutObjectResult result = ossClient.putObject(putObjectRequest);



        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
