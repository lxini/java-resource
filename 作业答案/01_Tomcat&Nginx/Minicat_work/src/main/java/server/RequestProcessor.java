package server;

import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class RequestProcessor extends Thread {

    private Socket socket;
    private Mapper mapper;

    public RequestProcessor(Socket socket, Mapper mapper) {
        this.socket = socket;
        this.mapper = mapper;
    }

    @Override
    public void run() {
        try{
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());

            Servlet servlet = resolveServlet(request.getUrl());
            // 静态资源处理
            if(servlet == null) {
                response.outputHtml(request.getUrl());
            }else{
                // 动态资源servlet请求
                servlet.service(request,response);
            }

            socket.close();

        }catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 从mapper中取出需要的servlet
     * @return
     */
    private Servlet resolveServlet(String url) {
        String[] split = url.split("/");
        String contextFlag = split[1];
        String servletFlag = url.substring(("/" + contextFlag).length());

        // 此处认为只有这一个host
        List<Context> contexts = mapper.getHosts().get(0).getContexts();
        for(Context context: contexts) {
            if(contextFlag.equalsIgnoreCase(context.getPath())) {
                List<Wrapper> wrappers = context.getWrappers();
                for(Wrapper wrapper:wrappers) {
                    if(wrapper.getUrlPattern().equalsIgnoreCase(servletFlag)) {
                        return wrapper.getServlet();
                    }
                }
            }
        }
        return null;
    }
}
