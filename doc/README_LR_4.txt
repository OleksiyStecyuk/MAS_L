1. �������� ������, ������ �����.
2. �����������: ������ ����� �������� � �������� ������

//���������� ������� ������, ��� ��������� ����� �������� � ����� � �������� ������
	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();


� ����� setup():

//����������� ������� �������� ��� ���� ������� 
		addBehaviour(tbf.wrap(new OfferRequestsServer()));// �������� ���������� ������ ������, �� ����������

		addBehaviour(tbf.wrap(new PurchaseOrdersServer()));// ������������ ����� � ������ ������
	