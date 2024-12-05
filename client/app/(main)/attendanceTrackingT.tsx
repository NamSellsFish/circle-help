import { FontAwesome } from "@expo/vector-icons";
import { FAB } from "@rneui/themed";
import { Router, Stack, useRouter } from "expo-router";
import React from "react";
import { Component } from "react";
import { TouchableOpacity, View, Text, StyleSheet, Pressable, Image, Alert } from "react-native";
import { Agenda, AgendaEntry, AgendaSchedule, DateData } from "react-native-calendars";
import { connect, useDispatch } from "react-redux";
import { attendanceState, dateToDateOnlyString, initialAttendanceState } from "~/store";
import { useGetAttendanceInfoItemsQuery } from "~/services";
import { getAttendance, updateAttendanceInfoItem } from "~/store";
import { useAppDispatch, useAppSelector, useDate } from "~/hooks";
import { dateToStringFormat } from "~/store";
import { AuthWrapper } from "~/components";

interface State {
    items?: AgendaSchedule;
    attendanceData?: object[];
}

interface Prop {
    attendance?: attendanceState;
    router?: Router;
    realTimeDate?: object;
}

function AttendanceTrackingScreen() {

    const router = useRouter();
    const realTimeDate = useDate();

    const attendance = useAppSelector((state) => state.attendance)

    const result = useGetAttendanceInfoItemsQuery({});

    const attendanceData: attendanceState[] = 
    (result.isSuccess) ? 
        result.data.map((it) => ({ ...it, currDate: new Date(it.currDate) })) 
    : 
        [];

    //alert("Fetched Data:  " + JSON.stringify(attendanceData));

    // TODO: Cần có 1 cái hàm để reset dữ liệu attendance (làm 1 lần thooi) khi qua ngày mới
    const todayAttendanceData: attendanceState = attendanceData.length === 0 ||
     attendanceData[0].currDate.getDate() != new Date().getDate() 
     ?attendance:attendanceData[0]    // attendanceData[0]&& result.isSuccess &&
    //  dateToDateOnlyString(attendanceData[0]?.currDate) ===
    //   dateToDateOnlyString(attendance?.currDate) 
    //   ? attendanceData[0] : attendance;

    //alert("Today Attendance Data: " + JSON.stringify(todayAttendanceData));

    const [items, setItems] = React.useState<AgendaSchedule>({});

    const loadItems = (day: DateData) => {

        setTimeout(() => {
            const newItems: AgendaSchedule = { ...items };
            attendanceData.forEach((data: attendanceState) => {
                const formattedCurrDate = dateToDateOnlyString(data.currDate);
                if (!newItems[formattedCurrDate]) {
                    newItems[formattedCurrDate] = [];
                    newItems[formattedCurrDate].push({
                        name: 'Item for ' + formattedCurrDate,
                        height: Math.max(50, Math.floor(Math.random() * 150)),
                        day: formattedCurrDate
                    });
                }
            });
            setItems(newItems);

        }, 500)

    }

    const renderDay = (day: any) => {
        if (day) {
            return <Text style={styles.customDay}>{day.getDay()}</Text>;
        }
        return <View style={styles.dayItem} />;
    };

    const renderItem = (reservation: AgendaEntry, isFirst: boolean) => {
        const fontSize = isFirst ? 16 : 14;
        const color = isFirst ? 'black' : '#43515c';
        const selectedAttendanceData = attendanceData.find((data) => 
                dateToDateOnlyString(data.currDate) === reservation.day 
        );
    
        
        return (
            <TouchableOpacity
                style={[styles.item, { height: 80 }]}
                onPress={() => Alert.alert(reservation.name)}
            >
                <View className="flex-row justify-between border-b-2 border-gray-400 border-dashed">
                    <Text style={{ fontSize, color }}>{reservation.day}</Text>
                    <Text style={{ fontSize, color: 'green' }}>{selectedAttendanceData?.type}</Text>
                </View>
                <View className="flex-row justify-between">
                    <View className="flex-col ">
                        <Text style={{ fontSize, color }}>Check In</Text>
                        <Text style={{ fontSize, color: 'gray', textAlign: 'center' }}>
                            {selectedAttendanceData?.punchInTime}
                        </Text>
                    </View>
                    <View className="flex-col">
                        <Text style={{ fontSize, color }}>Check Out</Text>
                        <Text style={{ fontSize, color: 'gray', textAlign: 'center' }}>
                            {selectedAttendanceData?.punchOutTime ?? initialAttendanceState.punchOutTime}
                        </Text>
                    </View>
                    <View className="flex-col">
                        <Text style={{ fontSize, color }}>Total Hour</Text>
                        <Text style={{ fontSize, color: 'gray', textAlign: 'center' }}>{selectedAttendanceData?.totalHours ?? initialAttendanceState.totalHours}</Text>
                    </View>
                </View>
            </TouchableOpacity>
        );
    };

    const renderEmptyDate = () => {
        return (
            <View style={styles.emptyDate}>
                <Text className="align-middle text-center">If this is not the weekend, it means that you are absent this day!</Text>
            </View>
        );
    };

    const rowHasChanged = (r1: AgendaEntry, r2: AgendaEntry) => {
        return r1.name !== r2.name;
    };

    function timeToString(time: number) {
        const date = new Date(time);
        return date.toISOString().split('T')[0];
    }

    const dispatch = useDispatch();

    React.useEffect(() => {
        //alert("Today Attendance Data: " + JSON.stringify(todayAttendanceData));
        dispatch(updateAttendanceInfoItem({ ...todayAttendanceData }));
    },[]);

    React.useEffect(() => {result.refetch()},[attendance])

    return (
        <>
            <Stack.Screen
                options={{
                    headerShown: true,
                    title: 'Welcome back, Root'
                }} />

            <AuthWrapper>
            {/* PunchIn PunchOut section */}
            <View className="flex-col justify-around h-[50%] bg-white">
                {/* Label */}
                <Text className="w-full text-center font-black text-2xl">{`${attendance?.type === 'PunchIn' ? 'Punch Out' : todayAttendanceData?.type === 'PunchOut' ? 'Out of work, You can rest now!' : 'Punch In'}`}</Text>
                {/* Button */}
                <Pressable onPress={async () => {
                        attendance?.type === 'PunchIn' ||
                        attendance?.type === undefined ||
                        attendance?.type === 'Late' ? 
                        router?.navigate('/imagePickerT') : null 
                    }} style={{ width: '50%', height: '50%', alignSelf: 'center' }}>
                    {
                        attendance?.type === 'PunchIn' ? <Image alt={"PunchIn/PunchOut Btn"} style={{ width: '100%', height: '100%', resizeMode: 'contain' }} source={require('../../assets/check-out-btn.png')} /> : attendance?.type === 'PunchOut' ? <Image alt={"PunchIn/PunchOut Btn"} style={{ width: '100%', height: '100%', resizeMode: 'contain' }} source={require('../../assets/check-in-btn-disable.png')} /> : <Image alt={"PunchIn/PunchOut Btn"} style={{ width: '100%', height: '100%', resizeMode: 'contain' }} source={require('../../assets/check-in-btn.png')} />
                    }
                </Pressable>

                {/* Today DateTime */}
                <View className="flex-col w-full">
                    <Text className="text-center text-gray-400 font-extrabold">{`${realTimeDate?.date}`}</Text>
                    <Text className="text-center font-bold">{`${realTimeDate?.time}`}</Text>
                </View>
                {/* Today Attendance Info */}
                <View className="flex-row justify-between">
                    <View className="flex-col align-middle pl-2">
                        <FontAwesome className="text-center" name="clock-o" size={24} color="green" />
                        <Text className="text-center font-semibold">{`${todayAttendanceData?.punchInTime}`}</Text>
                        <Text className="text-center font-semibold text-gray-500">Check In</Text>
                    </View>
                    <View className="flex-col align-middle pl-7">
                        <FontAwesome className="text-center" name="clock-o" size={24} color="red" />
                        <Text className="text-center font-semibold">
                            {`${todayAttendanceData?.punchOutTime ?? initialAttendanceState.punchOutTime}`}
                        </Text>
                        <Text className="text-center font-semibold text-gray-500">Check Out</Text>
                    </View>
                    <View className="flex-col align-middle pr-2">
                        <FontAwesome className="text-center" name="clock-o" size={24} color="blue" />
                        <Text className="text-center font-semibold">
                            {`${todayAttendanceData?.totalHours ?? initialAttendanceState.totalHours}`}
                        </Text>
                        <Text className="text-center font-semibold text-gray-500">Total Hour(s)</Text>
                    </View>
                </View>
            </View>
            <Text className="w-full text-center font-black bg-white text-2xl pt-5">History</Text>
            <Agenda 
                items={items}
                loadItemsForMonth={loadItems}
                selected={new Date()
                .toISOString().split('T')[0]}
                renderItem={renderItem}
                renderEmptyDate={renderEmptyDate}
                renderEmptyData={renderEmptyDate}
                rowHasChanged={rowHasChanged}
                showClosingKnob={true}
            />
            <FAB
                onPress={() => router.navigate('/chatbot')}
                visible={true}
                placement="right"
                icon={{ name: 'robot', color: 'white', type: 'material-community' }}
                color="green"
            />
            </AuthWrapper>
        </>
    );
}

const styles = StyleSheet.create({
    item: {
        backgroundColor: 'white',
        flex: 1,
        borderRadius: 5,
        padding: 10,
        marginRight: 10,
        marginTop: 17
    },
    emptyDate: {
        height: 15,
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        paddingTop: 30
    },
    customDay: {
        margin: 10,
        fontSize: 24,
        color: 'green'
    },
    dayItem: {
        marginLeft: 34
    }
});

export default AttendanceTrackingScreen;