package cho.nico.com.bluetoothm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class BluetoothUtils {


    private Context context;

    private BluetoothManager bluetoothManager;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothGatt bluetoothGatt;


    private static BluetoothUtils bluetoothUtils;


    private BluetoothUtils() {
        initManager();
    }

    public static BluetoothUtils getInstance() {
        if (bluetoothUtils == null) {
            bluetoothUtils = new BluetoothUtils();
        }
        return bluetoothUtils;

    }

    public void startScan(final BluetoothAdapter.LeScanCallback callback) {


        bluetoothAdapter.startLeScan(callback);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopScan(callback);
            }
        }, 10 * 1000);
    }


    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.stopLeScan(callback);
    }


    public void connectDevice(BluetoothDevice bluetoothDevice, BluetoothGattCallback callback) {

        bluetoothGatt = bluetoothDevice.connectGatt(context, true, callback, TRANSPORT_LE);
    }

    ;

    public void disConnectDevice() {
        bluetoothGatt.disconnect();
    }

    private BluetoothGattService getService(String serviceUUID) {

        UUID uuid = UUID.fromString(serviceUUID);
        BluetoothGattService service = bluetoothGatt.getService(uuid);
        return service;


        /*List<BluetoothGattService> services = bluetoothGatt.getServices();
        for (BluetoothGattService service : services) {
            if (service.getUuid().toString().equals(serviceUUID)) {
                return service;
            }
        }
        return null;*/
    }


    private BluetoothGattCharacteristic getCharacter(BluetoothGattService service, String characterUUID) {

        UUID uuid = UUID.fromString(characterUUID);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);

        return characteristic;
    }


    public void sendCommand(String serviceUUID, String characterUUID, byte[] command) {
        BluetoothGattCharacteristic characteristic = getOprCharacter(serviceUUID, characterUUID);

        if (characteristic == null) {
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, true);
        characteristic.setValue(command);
        bluetoothGatt.writeCharacteristic(characteristic);
    }


    public BluetoothGattCharacteristic getOprCharacter(String serviceUUID, String characterUUID) {

        BluetoothGattService service = getService(serviceUUID);
        if (service == null) {
            return null;
        } else {
            return getCharacter(service, characterUUID);
        }
    }

    public void initManager() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isBluetoothEnable() {
        return bluetoothAdapter.isEnabled();
    }
}
