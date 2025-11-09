import torch
import numpy as np

def observe(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    mean_train_losses = []
    mean_valid_losses = []
    valid_acc_list = []
    #optimizer = torch.optim.Adam(model.parameters(), lr=0.001)
    #loss_fn   = torch.nn.CrossEntropyLoss()

    for epoch in range(epochs):
        train_loss = 0.0
        train_loss2 = 0.0
        valid_loss = 0.0
        valid_loss2 = 0.0

        model.train()

        train_losses = []
        valid_losses = []
        for i, (input, labels) in enumerate(train_loader):

            #model.zero_grad()
            optimizer.zero_grad()
            #print(i)

            outputs = model(input)
            #print("sh", outputs, labels)
            #print("sh", outputs.shape, labels.shape)
            loss_outputs, loss_target = get_loss_inputs(config, outputs, labels)
            #print("sh", outputs.shape, labels.shape)
            #print("sh", outputs, labels)
            loss = loss_fn(loss_outputs, loss_target)

            loss = regularize(config, loss, model)

            loss.backward()
            optimizer.step()
            train_loss += loss.item()
            train_losses.append(loss.item())
            train_loss2 += loss.item() * input.size(0)

            #if (i * batch_size) % (batch_size * 100) == 0:
            #    print(f'{i * batch_size} / 50000')

        model.eval()
        correct = 0
        total = 0
        with torch.no_grad():
          #if False:
            for i, (input, labels) in enumerate(valid_loader):
                #print(i)
                outputs = model(input)
                loss_outputs, loss_target = get_loss_inputs(config, outputs, labels)
                loss = loss_fn(loss_outputs, loss_target)
                outputs_for_loss = outputs
                if False: #config.binary:
                    print("shape", outputs.shape, labels.shape)
                    outputs_for_loss = outputs.reshape(outputs.shape[0])
                    labels = labels.to(dtype=torch.float32).reshape(-1, 1).reshape(outputs.shape[0])
                    print("shape", outputs_for_loss.shape, labels.shape)
                    print("l", labels)
                    print("o", outputs_for_loss)
                    #outputs_for_loss = torch.round(outputs_for_loss)
                    #print("o", outputs_for_loss)
                   # labels = labels.to(torch.float)
                #loss = loss_fn(outputs_for_loss, labels)

                valid_losses.append(loss.item())
                valid_loss += loss.item()
                valid_loss2 += loss.item() * input.size(0)

                _, predicted = torch.max(outputs.data, 1)
                correct += (predicted == labels).sum().item()
                total += labels.size(0)

        mean_train_losses.append(np.mean(train_losses))
        mean_valid_losses.append(np.mean(valid_losses))

        train_loss /= len(train_loader)
        valid_loss /= len(valid_loader)
        train_loss2 /= len(train_loader.dataset)
        valid_loss2 /= len(valid_loader.dataset)
        #val_loss /= len(valid_loader.dataset)
        #print("len valid", len(valid_loader), len(valid_loader.dataset))

        accuracy = 100 * correct / total
        valid_acc_list.append(accuracy)
        print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
              .format(epoch + 1, train_loss2, valid_loss2, accuracy))
        #print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
        #      .format(epoch + 1, train_loss, valid_loss, accuracy))
        #print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
        #      .format(epoch + 1, np.mean(train_losses), np.mean(valid_losses), accuracy))

        torch.cuda.empty_cache()

        # Check early stopping condition
        # early_stopping.check_early_stop(val_loss)

        if early_stopping.stop_training:
            print(f"Early stopping at epoch {i}")
            break


def get_loss_inputs(config, outputs, labels):
    if config.binary:
        outputs = outputs.reshape(outputs.shape[0])
        # labels = labels.to(dtype=torch.float32).reshape(-1, 1).reshape(outputs.shape[0])
        # print("binary")
        labels = labels.to(torch.float)
    return outputs, labels


def regularize(config, loss, model):
    if config.regularize:
        regularization_type = 'L2'
        lambda_reg = 0.01
        # Apply L1 regularization
        if regularization_type == 'L1':
            l1_norm = sum(p.abs().sum() for p in model.parameters())
            loss += lambda_reg * l1_norm

        # Apply L2 regularization
        elif regularization_type == 'L2':
            l2_norm = sum(p.pow(2).sum() for p in model.parameters())
            loss += lambda_reg * l2_norm

        return loss
    return loss


# from github copilot

def observe_new(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    """Train/validation loop with correct accuracy computation and device handling.

    Improvements:
    - Move inputs/labels to the model's device.
    - Support binary (sigmoid + threshold) and multiclass (argmax) predictions.
    - Handle labels that are one-hot vectors by taking argmax.
    - Ensure labels have the correct dtype for loss (float for BCE, long for CrossEntropy).
    - Remove noisy debugging prints and make averages robust.
    """

    device = next(model.parameters()).device if any(p is not None for p in model.parameters()) else (torch.device('cuda') if torch.cuda.is_available() else torch.device('cpu'))

    mean_train_losses = []
    mean_valid_losses = []
    valid_acc_list = []

    for epoch in range(epochs):
        model.train()

        train_losses = []
        train_loss_sum = 0.0
        train_examples = 0

        # Training loop
        for i, (inputs, labels) in enumerate(train_loader):
            inputs = inputs.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()

            outputs = model(inputs)

            loss_inputs, loss_targets = get_loss_inputs_new(config, outputs, labels)
            loss = loss_fn(loss_inputs, loss_targets)
            loss = regularize_new(config, loss, model)

            loss.backward()
            optimizer.step()

            batch_size_effective = inputs.size(0)
            train_losses.append(loss.item())
            train_loss_sum += loss.item() * batch_size_effective
            train_examples += batch_size_effective

        # Validation loop
        model.eval()
        valid_losses = []
        valid_loss_sum = 0.0
        valid_examples = 0
        correct = 0
        total = 0

        with torch.no_grad():
            for i, (inputs, labels) in enumerate(valid_loader):
                inputs = inputs.to(device)
                labels = labels.to(device)

                outputs = model(inputs)

                loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
                loss = loss_fn(loss_inputs, loss_targets)

                batch_size_effective = inputs.size(0)
                valid_losses.append(loss.item())
                valid_loss_sum += loss.item() * batch_size_effective
                valid_examples += batch_size_effective

                # Prepare labels for accuracy check: convert one-hot to indices if needed
                labels_for_acc = labels
                if labels_for_acc.dim() > 1 and labels_for_acc.size(1) > 1:
                    # one-hot or probability vectors -> take argmax
                    labels_for_acc = torch.argmax(labels_for_acc, dim=1)
                labels_for_acc = labels_for_acc.view(-1).long()

                # Compute predictions according to binary/multiclass
                if getattr(config, 'binary', False):
                    # For binary, accept outputs that may have shape (N,1) or (N,)
                    probs = torch.sigmoid(outputs)
                    if probs.dim() > 1 and probs.size(1) == 1:
                        probs = probs.view(-1)
                    preds = (probs > 0.5).long()
                else:
                    # multiclass logits: take argmax along class dim
                    if outputs.dim() == 1 or (outputs.dim() > 1 and outputs.size(1) == 1):
                        # single-dim outputs -> treat as binary-like; round
                        preds = torch.round(outputs).long().view(-1)
                    else:
                        _, preds = torch.max(outputs, dim=1)

                correct += (preds == labels_for_acc).sum().item()
                total += labels_for_acc.size(0)

        # Compute averaged losses (guard against zero examples)
        avg_train_loss = (train_loss_sum / train_examples) if train_examples > 0 else float('nan')
        avg_valid_loss = (valid_loss_sum / valid_examples) if valid_examples > 0 else float('nan')
        mean_train_losses.append(np.mean(train_losses) if train_losses else float('nan'))
        mean_valid_losses.append(np.mean(valid_losses) if valid_losses else float('nan'))

        accuracy = 100.0 * (correct / total) if total > 0 else 0.0
        valid_acc_list.append(accuracy)

        print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
              .format(epoch + 1, avg_train_loss, avg_valid_loss, accuracy))

        torch.cuda.empty_cache()

        # Check early stopping condition
        if getattr(early_stopping, 'stop_training', False):
            print(f"Early stopping at epoch {epoch + 1}")
            break


def get_loss_inputs_new(config, outputs, labels):
    """Return (outputs_for_loss, labels_for_loss) with correct shapes and dtypes.

    - For binary tasks (config.binary==True): outputs are squeezed to (N,) if needed and
      labels are converted to float.
    - For multiclass tasks: labels are converted to Long tensors of class indices. If labels
      are provided as one-hot vectors, argmax is taken.
    """
    # Ensure labels are on same device as outputs
    labels = labels.to(device=outputs.device)

    if getattr(config, 'binary', False):
        # outputs may be (N,1) or (N,) -> make it (N,)
        if outputs.dim() > 1 and outputs.size(1) == 1:
            outputs = outputs.view(-1)
        labels_proc = labels.view(-1).to(dtype=torch.float)
        return outputs, labels_proc

    # Multiclass: if labels are one-hot/prob vectors, convert to indices
    labels_proc = labels
    if labels_proc.dim() > 1 and labels_proc.size(1) > 1:
        labels_proc = torch.argmax(labels_proc, dim=1)
    labels_proc = labels_proc.view(-1).long()

    return outputs, labels_proc


def regularize_new(config, loss, model):
    if getattr(config, 'regularize', False):
        regularization_type = 'L2'
        lambda_reg = 0.01
        # Apply L1 regularization
        if regularization_type == 'L1':
            l1_norm = sum(p.abs().sum() for p in model.parameters())
            loss = loss + lambda_reg * l1_norm

        # Apply L2 regularization
        elif regularization_type == 'L2':
            l2_norm = sum(p.pow(2).sum() for p in model.parameters())
            loss = loss + lambda_reg * l2_norm

        return loss
    return loss


def print_state_dict(model, optimizer):
    # Print model's state_dict
    print("Model's state_dict:")
    for param_tensor in model.state_dict():
        print(param_tensor, "\t", model.state_dict()[param_tensor].size())

    # Print optimizer's state_dict
    print("Optimizer's state_dict:")
    for var_name in optimizer.state_dict():
        print(var_name, "\t", optimizer.state_dict()[var_name])
