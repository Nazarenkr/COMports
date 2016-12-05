package ru.siberian.wolf;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Created by Siberian wolf on 10.11.2016.
 */

/*
*
*Данная программа управляет передачей данных между двумя сом портами. Для запуска программы необходимо наличие:
 *  1)Java SE Runtime Environment версии 1.7 и выше(скачать можно отсюда http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
 *  2)2 СОМ порта.
 * Чтобы запустить данную программу необходимо перейти в дерикторию с jar файлом данной программы и выполнить следующую команду:
 * java -jar comPorts-1.0-SNAPSHOT-jar-with-dependencies.jar args1 args2 args3 args4 args5
 * значение аргументов смотрите ниже.
 *  */
public class Laba2Async {
    private static SerialPort com1;
    private static SerialPort com2;
    public static int n = 2; //дефолтное значение колличества сеансов связи
    public static String message = "Siberia"; //дефолтное значение сообщения
    public static long time = 2000L; //дефолтное значение времени сна
    public static String firstPort = "COM2";//дефолтное имя первого порта
    public static String secondPort = "COM3";//дефолтное имя первого порта


    /**
     *
     * @param args
     * args 1 - колличество сеансов связи
     * args 2 - сообщения для обмена
     * args 3 - время сна, задается в миллисекундах
     * args 4 - имя первого порта
     * args 5 - имя второго порта
     *
     */

    public static void main(String[] args){
        //читаем аргументы, если они есть
        if (args.length > 0){
            switch (args.length){
                case 1: n = Integer.parseInt(args[0]); break;
                case 2: n = Integer.parseInt(args[0]); message = args[1]; break;
                case 3: n = Integer.parseInt(args[0]); message = args[1]; time = Long.parseLong(args[2]); break;
                case 4: n = Integer.parseInt(args[0]); message = args[1]; time = Long.parseLong(args[2]); firstPort= args[3]; break;
                case 5: n = Integer.parseInt(args[0]); message = args[1]; time = Long.parseLong(args[2]); firstPort= args[3]; secondPort= args[4]; break;

            }
        }
        String basisMessage = message;
        com1 = new SerialPort(firstPort);//создаем объект, отвечающий за порт СОМ1
        com2 = new SerialPort(secondPort);//создаем объект, отвечающий за порт СОМ2
        try{
            try {
                com1.openPort();//Пытаемся открыть порт. Если порт занят, то валимся с ошибкой.
            } catch (SerialPortException ex){
                System.out.println("Порт занят либо нет такого порта.");
            }
            try {
                com2.openPort();
            } catch (SerialPortException ex){
                System.out.println("Порт занят либо нет такого порта.");//Пытаемся открыть порт. Если порт занят, то валимся с ошибкой.
            }
            com1.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_5,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            com2.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_5,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            com1.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            com2.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
//            byte[] CDRIVES = hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d");
//            com2.writeBytes(CDRIVES);
            //создаем портридеры для наших портов. Порт ридер нужен, чтобы обрабатывать события.
            PortReader prtCom1Reader = new PortReader(com1);
            PortReader prtCom2Reader = new PortReader(com2);
            //разрешаем прерывания при сигнале Break
            com1.addEventListener(prtCom1Reader, SerialPort.MASK_BREAK);
            com2.addEventListener(prtCom2Reader, SerialPort.MASK_BREAK);
            //создаем события BreakInterrupt для портов
            SerialPortEvent breakCom1PortEvent = new SerialPortEvent(com1.getPortName(), SerialPortEvent.BREAK, 9);
            SerialPortEvent breakCom2PortEvent = new SerialPortEvent(com2.getPortName(), SerialPortEvent.BREAK, 9);
            //инициируем начало общения портов посылаем в первый порт сообщение
            com2.writeString("Start session");
            for (int i = 0; i < n; i++){
                message = basisMessage + "_" + i;
                prtCom1Reader.serialEvent(breakCom1PortEvent);
                prtCom2Reader.serialEvent(breakCom2PortEvent);
            }
            com1.readString();
            com2.readString();

        }catch (SerialPortException ex){
            System.out.println(ex);
        } finally {
            try {
                com1.closePort();//Пытаемся закрыть за собой порты. Чтобы процесс не висел.
                com2.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
    protected static class PortReader implements SerialPortEventListener {

        private SerialPort serialPort;

        public  PortReader( SerialPort serialPort) {
            this.serialPort = serialPort;
        }

        public void serialEvent(SerialPortEvent event) {
            if(event.isBREAK() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
//                    System.out.println( new String(serialPort.readBytes(event.getEventValue())));
                    System.out.println("From " + serialPort.getPortName() + " read next message: " +  serialPort.readString());
                    //Спим n секунд
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //И снова отправляем запрос
                    serialPort.writeString(message);
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
        //метод для конвертации формата данных String  в массив byte
        public  byte[] hexStringToByteArray(String s) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            return data;
        }
    }
}

