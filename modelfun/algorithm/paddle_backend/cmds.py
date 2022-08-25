import argparse
import pickle
from paddlecls.ernie_cls import erine_classification
from paddlecls.similarity import pretrained_lf
from paddlecls.ptunning_cls import ptunning_fewshot
from paddlener.ner import ner_model, ner_allinone, uie_finetune, uie_select


def erine_cls(input_files):
    with open(input_files, "rb") as get_myprofile:
        train_path, test_path, train_label, labeled_path, num_class, _ = pickle.load(get_myprofile)
    res = erine_classification(train_data_path=train_path, 
                        test_data_path= test_path, 
                        train_label_list=train_label,
                        labeled_path=labeled_path,
                        num_class=num_class
                        )
    print('classification done')
    # print(res)
    with open(input_files, "wb") as myprofile:  
        pickle.dump(res, myprofile)


def sim(input_files):
    with open(input_files, "rb") as get_myprofile:
        texts, model_name, examples, labels, oss, _ = pickle.load(get_myprofile)
    res = pretrained_lf(texts, model_name, examples, labels, oss=oss)
    with open(input_files, "wb") as myprofile:  
        pickle.dump(res, myprofile)

def ptune(input_files):
    with open(input_files, "rb") as get_myprofile:
        train_path, unlabeled_path, val_path, test_path, num_class, _ = pickle.load(get_myprofile)
    res = ptunning_fewshot(train_path=train_path, 
                           unlabeled_path=unlabeled_path, 
                           val_path=val_path,
                           test_path=test_path,
                           num_class=num_class)
    with open(input_files, "wb") as myprofile:  
        pickle.dump(res, myprofile)

def uietune(input_files):
    #uietune, uietrain, uielabeling
    with open(input_files, "rb") as get_myprofile:
        unlabeled_path, tune_path, test_path, model_name, schemas, _ = pickle.load(get_myprofile)
    res = uie_finetune(unlabeled_path, tune_path, test_path, model_name, schemas)
    with open(input_files, "wb") as myprofile:  
        pickle.dump(res, myprofile)
    
def uietrain(input_files):
    with open(input_files, "rb") as get_myprofile:
        unlabeled_path, train_path, test_path, model_name, schemas, _ = pickle.load(get_myprofile)
    res = uie_finetune(unlabeled_path = unlabeled_path, 
                        tune_path = train_path, 
                        test_path=test_path, 
                        model_name = model_name, 
                        schemas = schemas,
                        return_unlabeled=False,
                        uie_model='uie-base')
    with open(input_files, "wb") as myprofile:  
        pickle.dump(res, myprofile)

def uielabeling(input_files):
    with open(input_files, "rb") as get_myprofile:
        unlabeled_path, train_path, test_path, model_name, schemas, _ = pickle.load(get_myprofile)
    res = uie_finetune(unlabeled_path = unlabeled_path, 
                           tune_path = train_path, 
                           test_path=test_path, 
                           model_name = model_name, 
                           schemas = schemas)
    with open(input_files, "wb") as myprofile:  
        pickle.dump(res, myprofile)

def main(args):
    if args.api == 'erninecls':
        erine_cls(args.inputs)
    elif args.api == 'sim':
        sim(args.inputs)
    elif args.api == 'ptune':
        ptune(args.inputs)
    elif args.api == 'uietune':
        uietune(args.inputs)
    elif args.api == 'uietrain':
        uietrain(args.inputs)
    elif args.api == 'uielabeling':
        uielabeling(args.inputs)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument("--api", required=True, type=str, help="Which api to call")
    parser.add_argument("--inputs", type=str, required=True, help="Files to communicate with these process")
    args = parser.parse_args()
    main(args)