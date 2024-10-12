import { PropsWithChildren } from "react";
import { Modal, Specification } from "~/components";


type CompartmentModalProps = PropsWithChildren<{ isShow: boolean, onClose: () => void, data: object }>

export default function CompartmentModal({ isShow, onClose, data }: CompartmentModalProps) {


    return (
        <Modal isShow={isShow} onClose={onClose} moreClassNames="">
            <Modal.Content
                onClose={onClose}
                moreClassNames="flex flex-col px-5 py-3 bg-white rounded-lg gap-y-5 "
            >
                <Modal.Header moreClassNames="" onClose={onClose}>Product Detail</Modal.Header>
                <Modal.Body moreClassNames="">
                    <Specification specification={data} />
                </Modal.Body>
            </Modal.Content>
        </Modal>
    )
}