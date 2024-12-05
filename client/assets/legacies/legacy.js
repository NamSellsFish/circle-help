    const handleOptionTabChange = (value: number) => {
        setOptionNo(value);
        switch (value) {
            case 0:
                setArrangementOptionBody(OPTION_1);
                break;
            case 1:
                setArrangementOptionBody(MOCK_COMPARTMENT_PAIRS_AGAIN);
                break;
            case 2:
                setArrangementOptionBody(MOCK_COMPARTMENT_PAIRS);
                break;
            default:
                break;
        }
    }
                    <View style={{ flexDirection: 'column', flexWrap: 'wrap' }}>
                        <Tab
                            dense
                            value={optionNo}
                            onChange={handleOptionTabChange}
                            indicatorStyle={{
                                backgroundColor: 'green',
                                height: 3,
                            }}
                            buttonStyle={{
                                backgroundColor: 'brown',
                                height: 50,
                                width: '100%'
                            }}
                            variant="primary"

                        >
                            <Tab.Item
                                title="Option 1"
                                titleStyle={{ fontSize: 7 }}
                                icon={{ name: 'cog', type: 'ionicon', color: 'green' }}
                            />
                            <Tab.Item
                                title="Option 2"
                                titleStyle={{ fontSize: 7 }}
                                icon={{ name: 'cog', type: 'ionicon', color: 'green' }}
                            />
                            <Tab.Item
                                title="Option 3"
                                titleStyle={{ fontSize: 7 }}
                                icon={{ name: 'cog', type: 'ionicon', color: 'green' }}
                            />
                        </Tab>
                        <TabView value={0} onChange={() => { }} animationType="spring">
                            <TabView.Item style={{ width: '100%', height: '100%', flexDirection: 'column' }}>
                                <View className="flex flex-col w-full h-fit" >
                                    <Pressable onLongPress={handleOption1}>
                                        {/* @ts-ignore */}
                                        <JSONTree hideRoot={true} shouldExpandNode={() => true} data={arrangementOptionBody} />
                                    </Pressable>
                                    <ProductTable productsData={productsData} />
                                </View>
                            </TabView.Item>

                        </TabView>
                    </View>