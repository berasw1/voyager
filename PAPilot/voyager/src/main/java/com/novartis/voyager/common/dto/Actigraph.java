package com.novartis.voyager.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Actigraph {
    public final long qclJsonVersion;
    public final String protocol;
    public final DeviceDetails deviceDetails;
    public final Record records[];
    public final QclBtlePacketHeaders qclBtlePacketHeaders;
    public final TwonetProperties twonetProperties;

    @JsonCreator
    public Actigraph(@JsonProperty("qclJsonVersion") long qclJsonVersion, @JsonProperty("protocol") String protocol, @JsonProperty("deviceDetails") DeviceDetails deviceDetails, @JsonProperty("records") Record[] records, @JsonProperty("qclBtlePacketHeaders") QclBtlePacketHeaders qclBtlePacketHeaders, @JsonProperty("twonetProperties") TwonetProperties twonetProperties){
        this.qclJsonVersion = qclJsonVersion;
        this.protocol = protocol;
        this.deviceDetails = deviceDetails;
        this.records = records;
        this.qclBtlePacketHeaders = qclBtlePacketHeaders;
        this.twonetProperties = twonetProperties;
    }

    public static final class DeviceDetails {
        public final SerialNumber serialNumber;
        public final TimeOffsetFromGMT timeOffsetFromGMT;
        public final CurrentTimeOnDevice currentTimeOnDevice;
        public final HardfaultResets hardfaultResets;
        public final WatchdogResets watchdogResets;
        public final UnexpectedResets unexpectedResets;
        public final HaltState haltState;
        public final ErrorState errorState;
        public final BatteryState batteryState;
        public final DeviceState deviceState;
        public final SampleRate sampleRate;
        public final BatteryLevel batteryLevel;
        public final FirmwareBuildVersion firmwareBuildVersion;
        public final FirmwareMinorRevision firmwareMinorRevision;
        public final FirmwareMajorRevision firmwareMajorRevision;
        public final DecoderModel decoderModel;

        @JsonCreator
        public DeviceDetails(@JsonProperty("serialNumber") SerialNumber serialNumber, @JsonProperty("timeOffsetFromGMT") TimeOffsetFromGMT timeOffsetFromGMT, @JsonProperty("currentTimeOnDevice") CurrentTimeOnDevice currentTimeOnDevice, @JsonProperty("hardfaultResets") HardfaultResets hardfaultResets, @JsonProperty("watchdogResets") WatchdogResets watchdogResets, @JsonProperty("unexpectedResets") UnexpectedResets unexpectedResets, @JsonProperty("haltState") HaltState haltState, @JsonProperty("errorState") ErrorState errorState, @JsonProperty("batteryState") BatteryState batteryState, @JsonProperty("deviceState") DeviceState deviceState, @JsonProperty("sampleRate") SampleRate sampleRate, @JsonProperty("batteryLevel") BatteryLevel batteryLevel, @JsonProperty("firmwareBuildVersion") FirmwareBuildVersion firmwareBuildVersion, @JsonProperty("firmwareMinorRevision") FirmwareMinorRevision firmwareMinorRevision, @JsonProperty("firmwareMajorRevision") FirmwareMajorRevision firmwareMajorRevision, @JsonProperty("decoderModel") DecoderModel decoderModel){
            this.serialNumber = serialNumber;
            this.timeOffsetFromGMT = timeOffsetFromGMT;
            this.currentTimeOnDevice = currentTimeOnDevice;
            this.hardfaultResets = hardfaultResets;
            this.watchdogResets = watchdogResets;
            this.unexpectedResets = unexpectedResets;
            this.haltState = haltState;
            this.errorState = errorState;
            this.batteryState = batteryState;
            this.deviceState = deviceState;
            this.sampleRate = sampleRate;
            this.batteryLevel = batteryLevel;
            this.firmwareBuildVersion = firmwareBuildVersion;
            this.firmwareMinorRevision = firmwareMinorRevision;
            this.firmwareMajorRevision = firmwareMajorRevision;
            this.decoderModel = decoderModel;
        }

        public static final class SerialNumber {
            public final String value;
    
            @JsonCreator
            public SerialNumber(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class TimeOffsetFromGMT {
            public final long value;
            public final String unit;
    
            @JsonCreator
            public TimeOffsetFromGMT(@JsonProperty("value") long value, @JsonProperty("unit") String unit){
                this.value = value;
                this.unit = unit;
            }
        }

        public static final class CurrentTimeOnDevice {
            public final String value;
            public final String unit;
    
            @JsonCreator
            public CurrentTimeOnDevice(@JsonProperty("value") String value, @JsonProperty("unit") String unit){
                this.value = value;
                this.unit = unit;
            }
        }

        public static final class HardfaultResets {
            public final long value;
    
            @JsonCreator
            public HardfaultResets(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class WatchdogResets {
            public final long value;
    
            @JsonCreator
            public WatchdogResets(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class UnexpectedResets {
            public final long value;
    
            @JsonCreator
            public UnexpectedResets(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class HaltState {
            public final long value;
            public final String description;
    
            @JsonCreator
            public HaltState(@JsonProperty("value") long value, @JsonProperty("description") String description){
                this.value = value;
                this.description = description;
            }
        }

        public static final class ErrorState {
            public final long value;
            public final String description;
    
            @JsonCreator
            public ErrorState(@JsonProperty("value") long value, @JsonProperty("description") String description){
                this.value = value;
                this.description = description;
            }
        }

        public static final class BatteryState {
            public final long value;
            public final String description;
    
            @JsonCreator
            public BatteryState(@JsonProperty("value") long value, @JsonProperty("description") String description){
                this.value = value;
                this.description = description;
            }
        }
        
        public static final class DeviceState {
            public final long value;
            public final String description;
    
            @JsonCreator
            public DeviceState(@JsonProperty("value") long value, @JsonProperty("description") String description){
                this.value = value;
                this.description = description;
            }
        }

        public static final class SampleRate {
            public final long value;
            public final String unit;
    
            @JsonCreator
            public SampleRate(@JsonProperty("value") long value, @JsonProperty("unit") String unit){
                this.value = value;
                this.unit = unit;
            }
        }

        public static final class BatteryLevel {
            public final double value;
            public final String unit;
    
            @JsonCreator
            public BatteryLevel(@JsonProperty("value") double value, @JsonProperty("unit") String unit){
                this.value = value;
                this.unit = unit;
            }
        }

        public static final class FirmwareBuildVersion {
            public final long value;
    
            @JsonCreator
            public FirmwareBuildVersion(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class FirmwareMinorRevision {
            public final long value;
    
            @JsonCreator
            public FirmwareMinorRevision(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class FirmwareMajorRevision {
            public final long value;
    
            @JsonCreator
            public FirmwareMajorRevision(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class DecoderModel {
            public final String value;
    
            @JsonCreator
            public DecoderModel(@JsonProperty("value") String value){
                this.value = value;
            }
        }
    }

    public static final class Record {
        public final ActivityData activityData;
        public final Date date;
        public final IsCurrentLocalDay isCurrentLocalDay;
        public final PacketType packetType;

        @JsonCreator
        public Record(@JsonProperty("activityData") ActivityData activityData, @JsonProperty("date") Date date, @JsonProperty(value="isCurrentLocalDay", required=false) IsCurrentLocalDay isCurrentLocalDay, @JsonProperty("packetType") PacketType packetType){
            this.activityData = activityData;
            this.date = date;
            this.isCurrentLocalDay = isCurrentLocalDay;
            this.packetType = packetType;
        }

        public static final class ActivityData {
            public final Value value[];
    
            @JsonCreator
            public ActivityData(@JsonProperty("value") Value[] value){
                this.value = value;
            }
    
            public static final class Value {
                public final WearDetection wearDetection;
                public final StartTime startTime;
                public final CaloriesBurned caloriesBurned;
                public final Z z;
                public final Y y;
                public final X x;
                public final Steps steps;
                public final Interval interval;
        
                @JsonCreator
                public Value(@JsonProperty("wearDetection") WearDetection wearDetection, @JsonProperty("startTime") StartTime startTime, @JsonProperty("caloriesBurned") CaloriesBurned caloriesBurned, @JsonProperty("z") Z z, @JsonProperty("y") Y y, @JsonProperty("x") X x, @JsonProperty("steps") Steps steps, @JsonProperty("interval") Interval interval){
                    this.wearDetection = wearDetection;
                    this.startTime = startTime;
                    this.caloriesBurned = caloriesBurned;
                    this.z = z;
                    this.y = y;
                    this.x = x;
                    this.steps = steps;
                    this.interval = interval;
                }
        
                public static final class WearDetection {
                    public final long value;
                    public final String description;
            
                    @JsonCreator
                    public WearDetection(@JsonProperty("value") long value, @JsonProperty("description") String description){
                        this.value = value;
                        this.description = description;
                    }
                }
        
                public static final class StartTime {
                    public final String value;
                    public final String unit;
            
                    @JsonCreator
                    public StartTime(@JsonProperty("value") String value, @JsonProperty("unit") String unit){
                        this.value = value;
                        this.unit = unit;
                    }
                }
        
                public static final class CaloriesBurned {
                    public final long value;
                    public final String unit;
            
                    @JsonCreator
                    public CaloriesBurned(@JsonProperty("value") long value, @JsonProperty("unit") String unit){
                        this.value = value;
                        this.unit = unit;
                    }
                }
        
                public static final class Z {
                    public final long value;
            
                    @JsonCreator
                    public Z(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class Y {
                    public final long value;
            
                    @JsonCreator
                    public Y(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class X {
                    public final long value;
            
                    @JsonCreator
                    public X(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class Steps {
                    public final long value;
            
                    @JsonCreator
                    public Steps(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class Interval {
                    public final long value;
                    public final String unit;
            
                    @JsonCreator
                    public Interval(@JsonProperty("value") long value, @JsonProperty("unit") String unit){
                        this.value = value;
                        this.unit = unit;
                    }
                }
            }
        }

        public static final class Date {
            public final String value;
            public final String unit;
    
            @JsonCreator
            public Date(@JsonProperty("value") String value, @JsonProperty("unit") String unit){
                this.value = value;
                this.unit = unit;
            }
        }

        public static final class IsCurrentLocalDay {
            public final boolean value;
    
            @JsonCreator
            public IsCurrentLocalDay(@JsonProperty("value") boolean value){
                this.value = value;
            }
        }

        public static final class PacketType {
            public final String value;
    
            @JsonCreator
            public PacketType(@JsonProperty("value") String value){
                this.value = value;
            }
        }
    }

    public static final class QclBtlePacketHeaders {
        public final Packets packets;

        @JsonCreator
        public QclBtlePacketHeaders(@JsonProperty("packets") Packets packets){
            this.packets = packets;
        }

        public static final class Packets {
            public final Value value[];
    
            @JsonCreator
            public Packets(@JsonProperty("value") Value[] value){
                this.value = value;
            }
    
            public static final class Value {
                public final Version version;
                public final UuidDescription uuidDescription;
                public final Length length;
                public final Uuid uuid;
                public final Offset offset;
                public final UuidType uuidType;
                public final PacketType packetType;
                public final Id id;
        
                @JsonCreator
                public Value(@JsonProperty("version") Version version, @JsonProperty("uuidDescription") UuidDescription uuidDescription, @JsonProperty("length") Length length, @JsonProperty("uuid") Uuid uuid, @JsonProperty("offset") Offset offset, @JsonProperty("uuidType") UuidType uuidType, @JsonProperty("packetType") PacketType packetType, @JsonProperty("id") Id id){
                    this.version = version;
                    this.uuidDescription = uuidDescription;
                    this.length = length;
                    this.uuid = uuid;
                    this.offset = offset;
                    this.uuidType = uuidType;
                    this.packetType = packetType;
                    this.id = id;
                }
        
                public static final class Version {
                    public final long value;
            
                    @JsonCreator
                    public Version(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class UuidDescription {
                    public final String value;
            
                    @JsonCreator
                    public UuidDescription(@JsonProperty("value") String value){
                        this.value = value;
                    }
                }
        
                public static final class Length {
                    public final long value;
            
                    @JsonCreator
                    public Length(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class Uuid {
                    public final long value;
            
                    @JsonCreator
                    public Uuid(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class Offset {
                    public final long value;
            
                    @JsonCreator
                    public Offset(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class UuidType {
                    public final String value;
            
                    @JsonCreator
                    public UuidType(@JsonProperty("value") String value){
                        this.value = value;
                    }
                }
        
                public static final class PacketType {
                    public final long value;
            
                    @JsonCreator
                    public PacketType(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
        
                public static final class Id {
                    public final long value;
            
                    @JsonCreator
                    public Id(@JsonProperty("value") long value){
                        this.value = value;
                    }
                }
            }
        }
    }

    public static final class TwonetProperties {
        public final DeviceData deviceData;
        public final CustomerName customerName;
        public final HubId hubId;
        public final HubReceiveTimeOffset hubReceiveTimeOffset;
        public final TwonetId twonetId;
        public final CdeVersion cdeVersion;
        public final DeviceModel deviceModel;
        public final DeviceAddress deviceAddress;
        public final TimeZone timeZone;
        public final HubReceiveTime hubReceiveTime;
        public final AirInterfaceType airInterfaceType;
        public final SpReceiveTime spReceiveTime;
        public final CustomerId customerId;
        public final ExporterVersion exporterVersion;
        public final DeviceType deviceType;
        public final DeviceSerialNumber deviceSerialNumber;
        public final DecoderVersion decoderVersion;

        @JsonCreator
        public TwonetProperties(@JsonProperty("deviceData") DeviceData deviceData, @JsonProperty("customerName") CustomerName customerName, @JsonProperty("hubId") HubId hubId, @JsonProperty("hubReceiveTimeOffset") HubReceiveTimeOffset hubReceiveTimeOffset, @JsonProperty("twonetId") TwonetId twonetId, @JsonProperty("cdeVersion") CdeVersion cdeVersion, @JsonProperty("deviceModel") DeviceModel deviceModel, @JsonProperty("deviceAddress") DeviceAddress deviceAddress, @JsonProperty("timeZone") TimeZone timeZone, @JsonProperty("hubReceiveTime") HubReceiveTime hubReceiveTime, @JsonProperty("airInterfaceType") AirInterfaceType airInterfaceType, @JsonProperty("spReceiveTime") SpReceiveTime spReceiveTime, @JsonProperty("customerId") CustomerId customerId, @JsonProperty("exporterVersion") ExporterVersion exporterVersion, @JsonProperty("deviceType") DeviceType deviceType, @JsonProperty("deviceSerialNumber") DeviceSerialNumber deviceSerialNumber, @JsonProperty("decoderVersion") DecoderVersion decoderVersion){
            this.deviceData = deviceData;
            this.customerName = customerName;
            this.hubId = hubId;
            this.hubReceiveTimeOffset = hubReceiveTimeOffset;
            this.twonetId = twonetId;
            this.cdeVersion = cdeVersion;
            this.deviceModel = deviceModel;
            this.deviceAddress = deviceAddress;
            this.timeZone = timeZone;
            this.hubReceiveTime = hubReceiveTime;
            this.airInterfaceType = airInterfaceType;
            this.spReceiveTime = spReceiveTime;
            this.customerId = customerId;
            this.exporterVersion = exporterVersion;
            this.deviceType = deviceType;
            this.deviceSerialNumber = deviceSerialNumber;
            this.decoderVersion = decoderVersion;
        }
       
        public static final class DeviceData {
            public final String value;
    
            @JsonCreator
            public DeviceData(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class CustomerName {
            public final String value;
    
            @JsonCreator
            public CustomerName(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class HubId {
            public final String value;
    
            @JsonCreator
            public HubId(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class HubReceiveTimeOffset {
            public final long value;
    
            @JsonCreator
            public HubReceiveTimeOffset(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class TwonetId {
            public final String value;
    
            @JsonCreator
            public TwonetId(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class CdeVersion {
            public final String value;
    
            @JsonCreator
            public CdeVersion(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class DeviceModel {
            public final String value;
    
            @JsonCreator
            public DeviceModel(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class DeviceAddress {
            public final String value;
    
            @JsonCreator
            public DeviceAddress(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class TimeZone {
            public final String value;
    
            @JsonCreator
            public TimeZone(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class HubReceiveTime {
            public final long value;
    
            @JsonCreator
            public HubReceiveTime(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class AirInterfaceType {
            public final String value;
    
            @JsonCreator
            public AirInterfaceType(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class SpReceiveTime {
            public final long value;
    
            @JsonCreator
            public SpReceiveTime(@JsonProperty("value") long value){
                this.value = value;
            }
        }

        public static final class CustomerId {
            public final String value;
    
            @JsonCreator
            public CustomerId(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class ExporterVersion {
            public final String value;
    
            @JsonCreator
            public ExporterVersion(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class DecoderVersion{
       	 public final String value;
       	    
            @JsonCreator
            public DecoderVersion(@JsonProperty("value") String value){
                this.value = value;
            }
       }
        
        public static final class DeviceType {
            public final String value;
    
            @JsonCreator
            public DeviceType(@JsonProperty("value") String value){
                this.value = value;
            }
        }

        public static final class DeviceSerialNumber {
            public final String value;
    
            @JsonCreator
            public DeviceSerialNumber(@JsonProperty("value") String value){
                this.value = value;
            }
        }
    }
}