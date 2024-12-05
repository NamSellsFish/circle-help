import { FontAwesome } from "@expo/vector-icons";
import { FAB } from "@rneui/themed";
import { Router, Stack } from "expo-router";
import React from "react";
import { Component } from "react";
import { TouchableOpacity, View, Text, StyleSheet, Pressable, Image, Alert } from "react-native";
import { Agenda, AgendaEntry, AgendaSchedule, DateData } from "react-native-calendars";
import { connect } from "react-redux";
import { attendanceState } from "~/store";
import { formatToISODate } from "~/utils";
import { endpoints } from "~/services";
import { attendanceData } from "~/constants";
import { dateToStringFormat } from "~/store";

interface State {
    items?: AgendaSchedule;
    attendanceData?: object[];
}

interface Prop {
    attendance?: attendanceState;
    router?: Router;
    realTimeDate?: object;
}


const mapDispatch = {
    useGetAttendanceInfoItemsQuery: endpoints.getAttendanceInfoItems.initiate
};

export class AttendanceTrackingScreenClass extends Component<Prop, State> {
    state: State = {
        items: undefined,
    };

    attendanceData = []

    render() {

        this.attendanceData = attendanceData

        // TODO: Cần có 1 cái hàm để reset dữ liệu attendance (làm 1 lần thooi) khi qua ngày mới
        const todayAttendanceData: attendanceState = attendanceData && attendanceData[0]?.currDate === this.props.attendance?.currDate ? attendanceData[0] : this.props.attendance;

        return (
            <>
                <Stack.Screen
                    options={{
                        headerShown: true,
                        title: 'Welcome back, Root'
                    }} />

                {/* PunchIn PunchOut section */}
                <View className="flex-col justify-around h-[50%] bg-white">
                    {/* Label */}
                    <Text className="w-full text-center font-black text-2xl">{`${todayAttendanceData?.type === 'PunchIn' ? 'Punch Out' : todayAttendanceData?.type === 'PunchOut' ? 'Out of work, You can rest now!' : 'Punch In'}`}</Text>
                    {/* Button */}
                    <Pressable onPress={() => { todayAttendanceData?.type === 'PunchIn' || todayAttendanceData?.type === undefined ? this.props.router?.navigate('/imagePicker') : null }} style={{ width: '50%', height: '50%', alignSelf: 'center' }}>
                        {
                            todayAttendanceData?.type === 'PunchIn' ? <Image alt={"PunchIn/PunchOut Btn"} style={{ width: '100%', height: '100%', resizeMode: 'contain' }} source={require('../../assets/check-out-btn.png')} /> : todayAttendanceData?.type === 'PunchOut' ? <Image alt={"PunchIn/PunchOut Btn"} style={{ width: '100%', height: '100%', resizeMode: 'contain' }} source={require('../../assets/check-in-btn-disable.png')} /> : <Image alt={"PunchIn/PunchOut Btn"} style={{ width: '100%', height: '100%', resizeMode: 'contain' }} source={require('../../assets/check-in-btn.png')} />
                        }
                    </Pressable>

                    {/* Today DateTime */}
                    <View className="flex-col w-full">
                        <Text className="text-center text-gray-400 font-extrabold">{`${dateToStringFormat(todayAttendanceData?.currDate)}`}</Text>
                        <Text className="text-center font-bold">{`${this.props.realTimeDate?.time}`}</Text>
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
                            <Text className="text-center font-semibold">{`${todayAttendanceData?.punchOutTime}`}</Text>
                            <Text className="text-center font-semibold text-gray-500">Check Out</Text>
                        </View>
                        <View className="flex-col align-middle pr-2">
                            <FontAwesome className="text-center" name="clock-o" size={24} color="blue" />
                            <Text className="text-center font-semibold">{`${todayAttendanceData?.totalHours}`}</Text>
                            <Text className="text-center font-semibold text-gray-500">Total Hour(s)</Text>
                        </View>
                    </View>
                </View>
                <Text className="w-full text-center font-black bg-white text-2xl pt-5">History</Text>
                <Agenda
                    items={this.state.items}
                    loadItemsForMonth={this.loadItems}
                    selected={'2024-11-21'}
                    renderItem={this.renderItem}
                    renderEmptyDate={this.renderEmptyDate}
                    renderEmptyData={this.renderEmptyDate}
                    rowHasChanged={this.rowHasChanged}
                    showClosingKnob={true}
                />
                <FAB
                    onPress={() => this.props.router?.navigate('/chatbot')}
                    visible={true}
                    placement="right"
                    icon={{ name: 'robot', color: 'white', type: 'material-community' }}
                    color="green"
                />
            </>
        );
    }

    loadItems = (day: DateData) => {

        const items = this.state.items || {};
        setTimeout(() => {
            this.attendanceData.forEach((data) => {
                //data.currDate
                const formattedCurrDate = data.currDate;
                // ISSUE: Không hiểu vì sao lúc format lại bị trễ 1 ngày
                //alert(`Before: ${data.currDate} - After: ${formattedCurrDate}`);
                if (!items[formattedCurrDate]) {
                    items[formattedCurrDate] = [];
                    items[formattedCurrDate].push({
                        name: 'Item for ' + formattedCurrDate,
                        height: Math.max(50, Math.floor(Math.random() * 150)),
                        day: formattedCurrDate
                    });
                };
            });

            const newItems: AgendaSchedule = {};
            Object.keys(items).forEach(key => {
                newItems[key] = items[key];
            });
            this.setState({
                items: newItems
            });

        }, 500)

    }

    renderDay = (day: any) => {
        if (day) {
            return <Text style={styles.customDay}>{day.getDay()}</Text>;
        }
        return <View style={styles.dayItem} />;
    };

    renderItem = (reservation: AgendaEntry, isFirst: boolean) => {
        const fontSize = isFirst ? 16 : 14;
        const color = isFirst ? 'black' : '#43515c';
        const selectedAttendanceData = this.attendanceData.find((data) => formatToISODate(data.currDate) === reservation.day);
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
                        <Text style={{ fontSize, color: 'gray', textAlign: 'center' }}>{selectedAttendanceData?.punchInTime}</Text>
                    </View>
                    <View className="flex-col">
                        <Text style={{ fontSize, color }}>Check Out</Text>
                        <Text style={{ fontSize, color: 'gray', textAlign: 'center' }}>{selectedAttendanceData?.punchOutTime}</Text>
                    </View>
                    <View className="flex-col">
                        <Text style={{ fontSize, color }}>Total Hour</Text>
                        <Text style={{ fontSize, color: 'gray', textAlign: 'center' }}>{selectedAttendanceData?.totalHours}</Text>
                    </View>
                </View>
            </TouchableOpacity>
        );
    };

    renderEmptyDate = () => {
        return (
            <View style={styles.emptyDate}>
                <Text className="align-middle text-center">If this is not the weekend, it means that you are absent this day!</Text>
            </View>
        );
    };

    rowHasChanged = (r1: AgendaEntry, r2: AgendaEntry) => {
        return r1.name !== r2.name;
    };

    timeToString(time: number) {
        const date = new Date(time);
        return date.toISOString().split('T')[0];
    }
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



//const AttendanceTrackingScreen = connect(mapStateToProp, mapDispatch)(AttendanceTrackingScreenClass);

//export { AttendanceTrackingScreen };