package ru.siberian.wolf;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Created by Siberian wolf on 10.11.2016.
 */
public class Laba2Async {
    private static SerialPort com1;
    private static SerialPort com2;

    public static void main(String[] args){
        com1 = new SerialPort("COM1");//создаем объект, отвечающий за порт СОМ1
        com2 = new SerialPort("COM2");//создаем объект, отвечающий за порт СОМ2
        try{
            try {
                com1.openPort();//Пытаемся открыть порт. Если порт занят, то валимся с ошибкой.
            } catch (SerialPortException ex){
                System.out.println("Порт занят.");
            }
            try {
                com2.openPort();
            } catch (SerialPortException ex){
                System.out.println("Порт занят.");//Пытаемся открыть порт. Если порт занят, то валимся с ошибкой.
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
            com1.writeString("Get data" + "\n" + " Opachki");
            PortReader prtReader = new PortReader(com1);
            prtReader.serialEvent(new SerialPortEvent(com1.getPortName(), SerialPortEvent.RXCHAR, 1));
            com1.addEventListener(prtReader, SerialPort.MASK_RXCHAR);
            com1.writeString("Get data" + "\n" + " Opachki");
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
    private static class PortReader implements SerialPortEventListener {

        private SerialPort serialPort;

        public PortReader( SerialPort serialPort) {
            this.serialPort = serialPort;
        }

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    String data = serialPort.readString(event.getEventValue());
                    //И снова отправляем запрос
                    serialPort.writeString("Get data");
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}

