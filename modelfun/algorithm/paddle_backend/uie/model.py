import paddle
import paddle.nn as nn
from paddlenlp.transformers import ErniePretrainedModel


class UIE(ErniePretrainedModel):
    def __init__(self, encoding_model):
        super(UIE, self).__init__()
        self.encoder = encoding_model
        hidden_size = self.encoder.config["hidden_size"]
        self.linear_start = paddle.nn.Linear(hidden_size, 1)
        self.linear_end = paddle.nn.Linear(hidden_size, 1)
        self.sigmoid = nn.Sigmoid()

    def forward(self, input_ids, token_type_ids, pos_ids, att_mask):
        sequence_output, pooled_output = self.encoder(
            input_ids=input_ids,
            token_type_ids=token_type_ids,
            position_ids=pos_ids,
            attention_mask=att_mask)
        start_logits = self.linear_start(sequence_output)
        start_logits = paddle.squeeze(start_logits, -1)
        start_prob = self.sigmoid(start_logits)
        end_logits = self.linear_end(sequence_output)
        end_logits = paddle.squeeze(end_logits, -1)
        end_prob = self.sigmoid(end_logits)
        return start_prob, end_prob