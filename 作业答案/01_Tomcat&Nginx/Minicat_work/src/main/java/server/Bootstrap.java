package server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Minicat的主类
 */
public class Bootstrap {

    /**
     * 定义socket监听的端口号
     */
    private int port = 8080;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Minicat启动需要初始化展开的一些操作
     */
    public void start() throws Exception {

        // 加载解析相关的配置，web.xml
        loadConfig();


        // 定义一个线程池
        int corePoolSize = 10;
        int maximumPoolSize = 50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );

        ServerSocket serverSocket = new ServerSocket(port);

        /*
            多线程改造（使用线程池）
         */
        while (true) {

            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket, mapper);
            //requestProcessor.start();
            threadPoolExecutor.execute(requestProcessor);
        }


    }

    private Mapper mapper = new Mapper();

    /**
     * 加载解析web.xml，初始化Servlet
     */
    private void loadConfig() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            List<Element> selectNodes = rootElement.selectNodes("//Host");
            for (int i = 0; i < selectNodes.size(); i++) {

                Host host = new Host();

                Element element = selectNodes.get(i);
                String hostName = element.attributeValue("name");
                String appBase = element.attributeValue("appBase");
                // 扫描appBase下的文件夹，每一个文件夹认为是一个项目（Context）
                File appBaseFolder = new File(appBase);
                File[] files = appBaseFolder.listFiles();
                for(File file: files) {
                    if(file.isDirectory()) {
                        Context context = new Context();
                        String contextPath = file.getName();
                        context.setPath(contextPath);
                        // 构建Wrappers，一个Wrapper对应一个Servlet
                        File webFile = new File(file,"web.xml");
                        List<Wrapper> list = loadWebXml(webFile.getAbsolutePath());
                        context.setWrappers(list);
                        host.getContexts().add(context);
                    }
                }

                host.setName(hostName);
                mapper.getHosts().add(host);
            }

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * 解析web.xml,构建Wrappers
     */
    public List<Wrapper> loadWebXml(String webXmlPath) throws FileNotFoundException {

        List<Wrapper> list = new ArrayList<>();

        InputStream resourceAsStream = new FileInputStream(webXmlPath);
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            MyClassLoader myClassLoader = new MyClassLoader();
            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element = selectNodes.get(i);
                // <servlet-name>lagou</servlet-name>
                Element servletnameElement = (Element) element.selectSingleNode("servlet-name");
                String servletName = servletnameElement.getStringValue();
                // <servlet-class>server.LagouServlet</servlet-class>
                Element servletclassElement = (Element) element.selectSingleNode("servlet-class");
                String servletClass = servletclassElement.getStringValue();

                // 根据servlet-name的值找到url-pattern
                Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");

                // /lagou
                String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
                Class<?> aClass = myClassLoader.findClass(webXmlPath.replace("web.xml", "")  + "/",servletClass);
                HttpServlet servlet = (HttpServlet) aClass.newInstance();
                Wrapper wrapper = new Wrapper();
                wrapper.setUrlPattern(urlPattern);
                wrapper.setServlet(servlet);

                list.add(wrapper);


            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }



    /**
     * Minicat 的程序启动入口
     *
     * @param args
     */
    public static void main(String[] args) {

        Bootstrap bootstrap = new Bootstrap();
        try {
            // 启动Minicat
            bootstrap.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

