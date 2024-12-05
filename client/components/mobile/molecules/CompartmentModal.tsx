import { FontAwesome } from "@expo/vector-icons";
import { PropsWithChildren } from "react";
import { Modal, Specification } from "~/components";


type CompartmentModalProps = PropsWithChildren<{ isShow: boolean, onClose: () => void, data: object }>

export default function CompartmentModal({ isShow, onClose, data }: CompartmentModalProps) {

    return (
        <Modal isShow={isShow} onClose={onClose}>
            <Modal.Content
                onClose={onClose}
                moreClassNames="flex flex-col px-5 py-3 bg-white rounded-lg gap-y-5 "
            >
                <Modal.Header onClose={onClose} modalHeaderLabel="Product Detail">
                    <FontAwesome onPress={onClose} name="close" size={16} className="icon text-red-600" />
                </Modal.Header>
                <Modal.Body>
                    <Specification specification={data} />
                </Modal.Body>
            </Modal.Content>
        </Modal>
    )
}