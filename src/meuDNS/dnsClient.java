
        package meuDNS;

        import es.uvigo.det.ro.simpledns.*;

        import java.io.UnsupportedEncodingException;
        import java.net.*;
        import java.util.*;
        import java.io.DataInputStream;
        import java.io.DataOutputStream;
        import java.net.Socket;

public class dnsClient {

    private static int porto = 53;
    private static InetAddress ipDNS;
    private static TreeMap<String, Message> minhaCache = new TreeMap<>();

    private static List<ResourceRecord> seccionAUTHORITY;
    private static List<ResourceRecord> seccionADDITIONAL;
    private static List<ResourceRecord> seccionANSWER;

    private static AResourceRecord A;
    private static AAAAResourceRecord AAAA;
    private static NSResourceRecord NS;
    private static CNAMEResourceRecord CNAME;
    private static SOAResourceRecord SOA;
    private static MXResourceRecord MX;
    private static TXTResourceRecord TXT;

    /************************PRINCIPAL
     * @throws UnsupportedEncodingException **************************/
    public static void main(String[] args) throws Exception {
        if(args.length < 2)
        {
            System.out.println("\nMenor numero de argumentos dos esperados\n");
            System.exit(0);
        }

        String tp = args[0];
        ipDNS =  InetAddress.getByName(args[1]);

        /*****Eleximos o tipo de transporte ou UDP ou TCP*****/
        switch(tp)
        {
            case "-u":
                buscar("udp");
                break;
            case "-t":

                buscar("tcp");
                break;
            default:
                System.out.println("\nO argumento non é válido\n\n");
                System.exit(0);
                break;
        }

    }

/**************************************************************************************/

    /***************************TRANSPORTE UDP
     * @throws UnsupportedEncodingException **************************************/

    public static void buscar(String protocolo) throws Exception {
        RRType rrt = null;      //Gardamos o tipo da consulta A, AAAA ou NS
        String recurso = null;  //Gardamos o nome do dominio p.e. www.uvigo.es
        String recursoAgardar = null;
        boolean reintentar = false;
        boolean sair = false;
        InetAddress IPanterior=null;
        int contador = 0;
        int contadorAUX = 0;
        try {
            while (true) {
                InetAddress IP = ipDNS;  //A IP do servidor DNS ao que vamos consultar, a primeira e a que pasamos por argumentos
                System.out.println("\nPorfavor, introduza a súa busqueda\n\n");
                Scanner teclado = new Scanner(System.in);

                String peticion = teclado.nextLine();
                String[] aux = peticion.split(" ");

                switch (aux[0]) {
                    case "A":
                        rrt = RRType.A;
                        break;
                    case "NS":
                        rrt = RRType.NS;
                        break;
                    case "AAAA":
                        rrt = RRType.AAAA;
                        break;
                    case "CNAME":
                        rrt = RRType.CNAME;
                        break;
                    case "SOA":
                        rrt = RRType.SOA;
                        break;
                    case "MX":
                        rrt = RRType.MX;
                        break;
                    case "TXT":
                        rrt = RRType.TXT;
                        break;
                    default:
                        System.out.println("\nSó hai soporte para consultas de tipo A e NS\n\n");
                        break;
                }


                recurso = aux[1];
                recursoAgardar = recurso;
                if (minhaCache.containsKey(recurso)) {
                    System.out.println("\n" + "Q Cache " + rrt + " " + recurso);
                    seccionANSWER = minhaCache.get(recurso).getAnswers();
                    if (!seccionANSWER.isEmpty()) {
                        switch(rrt) {
                            case A:

                                A = (AResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " + A.getTTL() + " "  + A.getAddress().getHostAddress());
                                System.out.println();
                                break;

                            case AAAA:

                                AAAA = (AAAAResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " +AAAA.getTTL() + " "  +  AAAA.getAddress().getHostAddress());
                                System.out.println();
                                break;

                            case NS:

                                NS = (NSResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " + NS.getTTL() + " "  + NS.getNS());
                                System.out.println();
                                break;

                            case CNAME:
                                CNAME =(CNAMEResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " + CNAME.getTTL() + " "  + CNAME.getCNAME());
                                System.out.println();
                                break;

                            case SOA:
                                SOA =(SOAResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " "+ SOA.getTTL() + " " + SOA.getMaster() + " "+ SOA.getHostmaster()+ " " + SOA.getSerial() + " "+ SOA.getRefresh() + " "+ SOA.getRetry() +  " "+ SOA.getExpire() + " " + SOA.getMinimum() );
                                System.out.println();
                                break;

                            case MX:
                                MX =(MXResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " "+ MX.getTTL() + "  " + MX.getMXprioridad() +" " +MX.getMXservidor());
                                System.out.println();
                                break;
                        }
                        continue;
                    }
                    else{
                        System.out.println("Non existe resposta");
                        continue;
                    }

                }
                InetAddress ip = null;
                Inet6Address ip6 = null;
                boolean fin = false;
                while (true) {
                    sair = false;
                    Message mensaxe = null;

                    if (protocolo.equals("udp")) {
                        System.out.println("Q UDP " + IP.getHostAddress() + " " + rrt + " " + recurso); //Mensaxe de consulta
                        mensaxe = UDP(IP, rrt, recurso); //Obtemos a resposta do DNS
                    }
                    if (protocolo.equals("tcp")) {
                        System.out.println("Q TCP " + IP.getHostAddress() + " " + rrt + " " + recurso); //Mensaxe de consulta
                        mensaxe = TCP(IP, rrt, recurso); //Obtemos a resposta do DNS
                    }
                    /****OBTEMOS AS SECCIONS DA RESPOSTA DO DNS*****/
                    seccionANSWER = mensaxe.getAnswers();               //Conten a resposta coa ip final
                    seccionAUTHORITY = mensaxe.getNameServers();        //Conten o nome de servidores DNS aos que lle podemos preguntar
                    seccionADDITIONAL = mensaxe.getAdditonalRecords();  //Conten as ips dos servidores da seccion AUTHORITY, as cales usaremos para obter a ip desexada


                    if (!seccionANSWER.isEmpty())                                         //Se non obtemos a ip final, necesitamos preguntar a outro dns
                    {

                        switch(rrt) {
                            case A:
                                if (seccionANSWER.get(0).getRRType().equals(RRType.CNAME)) {
                                    CNAME = (CNAMEResourceRecord) seccionANSWER.get(0);
                                    recurso = CNAME.getCNAME().toString();
                                    IP = ipDNS;
                                    System.out.println();
                                    continue;
                                }
                                A = (AResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " + A.getTTL() + " "  + A.getAddress().getHostAddress());
                                minhaCache.put(recursoAgardar, mensaxe);
                                System.out.println();
                                break;

                            case AAAA:
                                if (seccionANSWER.get(0).getRRType().equals(RRType.CNAME)) {
                                    CNAME = (CNAMEResourceRecord) seccionANSWER.get(0);
                                    recurso = CNAME.getCNAME().toString();
                                    IP = ipDNS;
                                    System.out.println();
                                    continue;
                                }
                                AAAA = (AAAAResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " +AAAA.getTTL() + " "  +  AAAA.getAddress().getHostAddress());
                                minhaCache.put(recursoAgardar, mensaxe);
                                System.out.println();
                                break;

                            case NS:
                                if (seccionANSWER.get(0).getRRType().equals(RRType.CNAME)) {
                                    CNAME = (CNAMEResourceRecord) seccionANSWER.get(0);
                                    recurso = CNAME.getCNAME().toString();
                                    IP = ipDNS;
                                    System.out.println();
                                    continue;
                                }
                                NS = (NSResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " + NS.getTTL() + " "  + NS.getNS());
                                minhaCache.put(recursoAgardar, mensaxe);
                                System.out.println();
                                break;

                            case CNAME:
                                CNAME =(CNAMEResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " " + CNAME.getTTL() + " "  + CNAME.getCNAME());
                                minhaCache.put(recursoAgardar, mensaxe);
                                System.out.println();
                                break;

                            case SOA:
                                if (seccionANSWER.get(0).getRRType().equals(RRType.CNAME)) {
                                    CNAME = (CNAMEResourceRecord) seccionANSWER.get(0);
                                    recurso = CNAME.getCNAME().toString();
                                    IP = ipDNS;
                                    System.out.println();
                                    continue;
                                }
                                SOA =(SOAResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " "+ SOA.getTTL() + " " + SOA.getMaster() + " "+ SOA.getHostmaster()+ " " + SOA.getSerial() + " "+ SOA.getRefresh() + " "+ SOA.getRetry() +  " "+ SOA.getExpire() + " " + SOA.getMinimum() );
                                minhaCache.put(recursoAgardar, mensaxe);
                                System.out.println();
                                break;

                            case MX:
                                if (seccionANSWER.get(0).getRRType().equals(RRType.CNAME)) {
                                    CNAME = (CNAMEResourceRecord) seccionANSWER.get(0);
                                    recurso = CNAME.getCNAME().toString();
                                    IP = ipDNS;
                                    System.out.println();
                                    continue;
                                }
                                MX =(MXResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " "+ MX.getTTL() + "  " + MX.getMXprioridad() +" " +MX.getMXservidor());
                                minhaCache.put(recursoAgardar, mensaxe);
                                System.out.println();
                                break;

                            case TXT:
                                if (seccionANSWER.get(0).getRRType().equals(RRType.CNAME)) {
                                    CNAME = (CNAMEResourceRecord) seccionANSWER.get(0);
                                    recurso = CNAME.getCNAME().toString();
                                    IP = ipDNS;
                                    System.out.println();
                                    continue;
                                }
                                TXT =(TXTResourceRecord) seccionANSWER.get(0);
                                System.out.println("A " + IP.getHostAddress() + " "+ TXT.getTTL() + " " + TXT.getTXT());
                                minhaCache.put(recursoAgardar, mensaxe);
                                System.out.println();
                                break;

                        }

                        break;
                    } else {

                        if (!seccionAUTHORITY.isEmpty()) {
                            RRType tipo = null;
                            int ttl = 0;
                            DomainName dominio = null;

                            if(seccionAUTHORITY.get(0).getRRType().equals(RRType.NS)) {
                                NS = (NSResourceRecord) seccionAUTHORITY.get(0);

                                 tipo = NS.getRRType();

                                 ttl = NS.getTTL();
                                 dominio = NS.getNS();

                                System.out.println("A " + IP.getHostAddress() + " " + tipo + " " + ttl + " " + dominio);
                            }
                            if(seccionAUTHORITY.get(0).getRRType().equals(RRType.SOA)) {
                               SOA = (SOAResourceRecord) seccionAUTHORITY.get(0);

                                 tipo = SOA.getRRType();

                                 ttl = SOA.getTTL();
                                 dominio = SOA.getMaster();

                                System.out.println("A " + IP.getHostAddress() + " " + tipo + " " + ttl + " " + dominio);
                            }
                            boolean nonAddi = false;
                            if (!seccionADDITIONAL.isEmpty()) {
                                for (int i = 0; i < seccionADDITIONAL.size(); i++) {
                                    tipo = seccionADDITIONAL.get(i).getRRType();
                                    ttl = seccionADDITIONAL.get(i).getTTL();
                                    if (tipo == RRType.A) {
                                        A = (AResourceRecord) seccionADDITIONAL.get(i);
                                        ip = A.getAddress();
                                    }
                                    if (tipo == RRType.AAAA) {
                                        AAAA = (AAAAResourceRecord) seccionADDITIONAL.get(i);
                                        ip = AAAA.getAddress();
                                    }

                                        if (dominio.toString().equals(seccionADDITIONAL.get(i).getDomain().toString())) {
                                            System.out.println("A " + IP.getHostAddress() + " " + tipo + " " + ttl + " " + ip);
                                            nonAddi = true;
                                            if (tipo == RRType.AAAA)
                                              ip = InetAddress.getByName(NS.getNS().toString());

                                            System.out.println();
                                            break;
                                        }


                                }
                            }
                            if (seccionADDITIONAL.isEmpty() || !nonAddi) {

                                if(IP.equals(InetAddress.getByName(NS.getNS().toString())))
                                {
                                    System.out.println("Non existe resposta");
                                    minhaCache.put(recursoAgardar, mensaxe);
                                    System.out.println();
                                    break;
                                }
                                System.out.println("Non existen rexistros tipo A na sección ADDITIONAL para " + dominio);

                                IP =  InetAddress.getByName(NS.getNS().toString());
                                System.out.println();
                                fin = true;
                                continue;
                            } else
                                IP = ip;
                        }


                    }



                }

            }
            }catch(ArrayIndexOutOfBoundsException e)
            {
                System.out.println("\nMenor numero de argumentos do esperado\n\n");
            }

    }

    public static Message UDP(InetAddress dir_ip, RRType rrt, String recurso) throws Exception
    {

        Message m_resposta = null;


        Message m = new Message(recurso, rrt, false); //E a consulta que queremos enviar ao DNS. O false é para desactivar o flag rd (recursion)

        byte[] m_consulta = m.toByteArray(); //Transformamos o message a un array de bytes, para transportalo pola rede
        DatagramSocket ds = new DatagramSocket(); //Creamos o noso socket
        DatagramPacket meuPaquete = new DatagramPacket(m_consulta, m_consulta.length, dir_ip, porto); //Creamos o noso paquete

        ds.send(meuPaquete); //Enviamos o paquete

        byte[] buffer = new byte[1024];
        DatagramPacket resposta = new DatagramPacket(buffer, buffer.length); //construimos o paquete de reposta

        ds.receive(resposta); //recibimos a resposta do DNS


        ds.close();
        try {
            m_resposta = new Message(buffer); //pasamos a resposta a un message m_resposta = new Message(buffer); //pasamos a resposta a un message
        } catch (Exception e) {

            if (e.getMessage() == "We do not know what to do with truncated responses") {
                m_resposta = TCP(dir_ip, rrt, recurso);

            }
        }
        return m_resposta;
    }

    public static Message TCP(InetAddress dir_ip, RRType rrt, String recurso) throws Exception
    {

        Message resposta = null;

        Message m = new Message(recurso, rrt, false);
        Socket so = new Socket(dir_ip, 53);

        DataInputStream entrada = new DataInputStream(so.getInputStream());
        DataOutputStream saida = new DataOutputStream(so.getOutputStream());

        byte[] lonxitude = Utils.int16toByteArray(m.toByteArray().length);
        saida.write(lonxitude);
        saida.write(m.toByteArray());
        saida.flush();
        byte[] longitudeRespostaBytes = new byte[2];
        entrada.readFully(longitudeRespostaBytes);
        int lonxitudeResposta = Utils.int16fromByteArray(longitudeRespostaBytes);
        byte[] respuesta_bytes = new byte[lonxitudeResposta];
        entrada.readFully(respuesta_bytes);

        resposta = new Message(respuesta_bytes);

        entrada.close();
        saida.close();
        so.close();

        return resposta;

    }



}
