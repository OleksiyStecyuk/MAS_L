1. �������� ������, ������ �����.
2. �����������: ���� "BrokerAgent"
���� ������ ������� checkOffer ��� � ����������� ������� ������� True ��� False, 
���� ��������� True - ��� ������ ������ ������ �� ������ ��� �� ����������. 
������ ������ ����������� Reject, ��� �������� ������ ����������.

������� ���:
ACLMessage agree = request.createReply();
if (checkOffer())
{
System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
agree.setPerformative(ACLMessage.AGREE);
}
else
{
System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is REJECTED. OFFER IS TOO BAD!!");
agree.setPerformative(ACLMessage.REJECT_PROPOSAL);
}
